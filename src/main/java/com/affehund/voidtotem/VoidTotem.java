package com.affehund.voidtotem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.affehund.voidtotem.core.ModDataGeneration;
import com.affehund.voidtotem.core.ModUtils;
import com.affehund.voidtotem.core.VoidTotemConfig;
import com.affehund.voidtotem.core.network.PacketHandler;
import com.affehund.voidtotem.core.network.TotemEffectPacket;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Rarity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

/**
 * @author Affehund
 *
 */
@Mod(ModConstants.MOD_ID)
public class VoidTotem {

	public static final Logger LOGGER = LogManager.getLogger(ModConstants.MOD_NAME);

	public static VoidTotem INSTANCE;

	final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
	final IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

	public VoidTotem() {
		INSTANCE = this;
		LOGGER.debug("Loading up " + ModConstants.MOD_NAME + "!");

		modEventBus.addListener(this::gatherData);
		modEventBus.addListener(this::enqueueIMC);
		ITEMS.register(modEventBus);

		forgeEventBus.register(this);
		forgeEventBus.addListener(this::livingHurt);
		forgeEventBus.addListener(this::livingFall);
		forgeEventBus.addListener(this::playerTick);

		PacketHandler.registerMessages();
		ModLoadingContext.get().registerConfig(Type.COMMON, VoidTotemConfig.COMMON_CONFIG_SPEC,
				ModConstants.COMMON_CONFIG_NAME);
	}

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			ModConstants.MOD_ID);

	public static final RegistryObject<Item> VOID_TOTEM_ITEM = ITEMS.register(ModConstants.VOID_TOTEM_STRING,
			() -> new Item(new Item.Properties().maxStackSize(1).group(ItemGroup.COMBAT).rarity(Rarity.UNCOMMON)));

	private void enqueueIMC(final InterModEnqueueEvent event) {
		if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) {
			InterModComms.sendTo(ModConstants.CURIOS_MOD_ID, SlotTypeMessage.REGISTER_TYPE,
					() -> SlotTypePreset.CHARM.getMessageBuilder().build());
			VoidTotem.LOGGER.info("Enqueued IMC to {}", ModConstants.CURIOS_MOD_ID);
		}
	}

	private void gatherData(final GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		if (event.includeServer()) {
			generator.addProvider(new ModDataGeneration.RecipeGen(generator));
			BlockTagsProvider blockTagsProvider = new ModDataGeneration.BlockTagsGen(generator, ModConstants.MOD_ID,
					existingFileHelper);
			generator.addProvider(new ModDataGeneration.ItemTagsGen(generator, blockTagsProvider, ModConstants.MOD_ID,
					existingFileHelper));
		}
		if (event.includeClient()) {
			generator.addProvider(new ModDataGeneration.LanguageGen(generator, ModConstants.MOD_ID, "de_de"));
			generator.addProvider(new ModDataGeneration.LanguageGen(generator, ModConstants.MOD_ID, "en_us"));
			generator.addProvider(
					new ModDataGeneration.ItemModelGen(generator, ModConstants.MOD_ID, existingFileHelper));
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void tooltip(final ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (stack.getItem() == VoidTotem.VOID_TOTEM_ITEM.get()) {
			event.getToolTip().add(
					new TranslationTextComponent(ModConstants.VOID_TOTEM_TOOLTIP).mergeStyle(TextFormatting.GREEN));
		}
	}

	private void playerTick(PlayerTickEvent event) {
		if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.START) {
			final BlockPos playerPos = event.player.getPosition();
			if (event.player.getPersistentData().getBoolean(ModConstants.NBT_TAG)) {
				if (event.player instanceof ServerPlayerEntity) {
					((ServerPlayerEntity) event.player).connection.floatingTickCount = 0;
					if (event.player.isInWater() || event.player.abilities.isFlying
							|| event.player.abilities.allowFlying
							|| event.player.world.getBlockState(playerPos).getBlock() == Blocks.COBWEB) {
						event.player.getPersistentData().putBoolean(ModConstants.NBT_TAG, false);
						return;
					}
				}
			}
		}
	}

	private void livingHurt(LivingHurtEvent event) {
		if (event.getEntity().world.isRemote()) // is client
			return;
		if (VoidTotemConfig.COMMON_CONFIG.BLACKLISTED_DIMENSIONS.get()
				.contains(event.getEntityLiving().world.getDimensionKey().getLocation().toString())) // dim on blacklist
			return;
		// no valid damage
		if (event.getSource() != DamageSource.OUT_OF_WORLD)
			return;
		// important: player has to be below y=-64 (else /kill command wouldn't work
		// when totem equiped)
		if (event.getEntityLiving().getPosY() > -64)
			return;
		if (event.getEntityLiving() instanceof ServerPlayerEntity) { // is server player entity
			ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
			player.connection.floatingTickCount = 0;
			if (event.getAmount() < player.getHealth()) // only run if player about to die
				return;
			ItemStack itemstack = null;
			boolean foundValidStack = false;
			if (!VoidTotemConfig.COMMON_CONFIG.NEEDS_TOTEM.get()) {
				itemstack = ItemStack.EMPTY;
				foundValidStack = true;
			} else {
				if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) {
					ItemStack curiosVoidTotemStack = ModUtils.findCuriosItem(VoidTotem.VOID_TOTEM_ITEM.get(), player);
					ItemStack curiosVanillaTotemStack = VoidTotemConfig.COMMON_CONFIG.ALLOW_TOTEM_OF_UNDYING.get()
							? ModUtils.findCuriosItem(Items.TOTEM_OF_UNDYING, player)
							: ItemStack.EMPTY;
					if (curiosVoidTotemStack != ItemStack.EMPTY) {
						itemstack = copyAndRemoveItemStack(curiosVoidTotemStack, player);
						foundValidStack = true;
					} else if (curiosVanillaTotemStack != ItemStack.EMPTY) {
						itemstack = copyAndRemoveItemStack(curiosVanillaTotemStack, player);
						foundValidStack = true;
					}
				}

				if (!foundValidStack) {
					for (Hand hand : Hand.values()) { // for each hand
						ItemStack itemStackHand = player.getHeldItem(hand);
						boolean isVoidTotem = itemStackHand.getItem() == VoidTotem.VOID_TOTEM_ITEM.get();
						boolean isTotemOfUndying = VoidTotemConfig.COMMON_CONFIG.ALLOW_TOTEM_OF_UNDYING.get()
								? itemStackHand.getItem() == Items.TOTEM_OF_UNDYING
								: false;
						if (isVoidTotem || isTotemOfUndying) { // is valid item
							itemstack = copyAndRemoveItemStack(itemStackHand, player);
							foundValidStack = true;
							break;
						}
					}
				}
			}

			if (itemstack != null && foundValidStack) { // check if stack is null
				if (player.connection.targetPos != null) // wants to teleport
					return;
				if (player.isBeingRidden()) { // has passenger and remove it
					player.removePassengers();
				}
				player.stopRiding();

				boolean teleportedToBlock = false;
				for (int i = 0; i < 16; i++) { // try 16 times
					double x = player.getPosX() + (player.getRNG().nextDouble() - 0.5D) * 2.0D;
					double y = MathHelper.clamp(player.getRNG().nextInt() * player.world.getHeight(), 0.0D,
							player.world.getHeight() - 1);
					double z = player.getPosZ() + (player.getRNG().nextDouble() - 0.5D) * 2.0;

					if (player.attemptTeleport(x, y, z, true)) { // if can teleport break
						teleportedToBlock = true;
						break;
					}
				}

				if (!teleportedToBlock) { // if can't teleport to a block teleport to height set in config
					player.setPositionAndUpdate(player.getPosX(), VoidTotemConfig.COMMON_CONFIG.TELEPORT_HEIGHT.get(),
							player.getPosZ());
					player.connection.floatingTickCount = 0;
				}

				event.setCanceled(true);
				player.getPersistentData().putBoolean(ModConstants.NBT_TAG, true); // add tag to prevent fall damage

				PacketHandler.sendToPlayer(new TotemEffectPacket(itemstack, player), player); // totem effect packet
				PacketHandler.sendToAllTracking(new TotemEffectPacket(itemstack, player), player); // to all tracking
			}
		}
	}

	private static ItemStack copyAndRemoveItemStack(ItemStack itemStack, ServerPlayerEntity player) {
		ItemStack itemStackCopy = itemStack.copy();
		if (itemStack != ItemStack.EMPTY || itemStack != null) { // add stats if not null / empty
			player.addStat(Stats.ITEM_USED.get(itemStack.getItem()));
		}
		itemStack.shrink(1);
		return itemStackCopy;
	}

	private void livingFall(LivingFallEvent event) {
		if (event.getEntity() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
			if (player.getPersistentData().getBoolean(ModConstants.NBT_TAG)) { // if has tag
				player.connection.floatingTickCount = 0;
				event.setDamageMultiplier(0f); // set damage multiplier to 0 => no damage
				player.getPersistentData().putBoolean(ModConstants.NBT_TAG, false); // remove tag
				event.setCanceled(true);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void playActivateAnimation(ItemStack stack, Entity entity) {
		Minecraft mc = Minecraft.getInstance();
		mc.particles.emitParticleAtEntity(entity, ParticleTypes.TOTEM_OF_UNDYING, 30); // particles
		mc.world.playSound(entity.getPosX(), entity.getPosY(), entity.getPosZ(), SoundEvents.ITEM_TOTEM_USE,
				entity.getSoundCategory(), 1.0F, 1.0F, false); // sound

		if (entity == mc.player) {
			mc.gameRenderer.displayItemActivation(stack); // animation
		}
	}

	public static ResourceLocation getModResourceLocation(String path) {
		return new ResourceLocation(ModConstants.MOD_ID, path);
	}
}
