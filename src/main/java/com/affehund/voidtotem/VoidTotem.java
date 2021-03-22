package com.affehund.voidtotem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.affehund.voidtotem.core.ModDataGeneration;
import com.affehund.voidtotem.core.ModUtils;
import com.affehund.voidtotem.core.VoidTotemConfig;
import com.affehund.voidtotem.core.network.PacketHandler;
import com.affehund.voidtotem.core.network.TotemEffectPacket;

import net.minecraft.advancements.CriteriaTriggers;
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
import net.minecraft.loot.LootPool;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.event.LootTableLoadEvent;
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
 * The main mod class.
 * 
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
		LOGGER.debug("Loading up {}!", ModConstants.MOD_NAME);

		modEventBus.addListener(this::gatherData);
		modEventBus.addListener(this::enqueueIMC);
		ITEMS.register(modEventBus);

		forgeEventBus.register(this);
		forgeEventBus.addListener(this::livingHurt);
		forgeEventBus.addListener(this::livingFall);
		forgeEventBus.addListener(this::playerTick);

		// register of the messages
		PacketHandler.registerMessages();
		// register of the config
		ModLoadingContext.get().registerConfig(Type.COMMON, VoidTotemConfig.COMMON_CONFIG_SPEC,
				ModConstants.COMMON_CONFIG_NAME);
	}

	// deferred register
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			ModConstants.MOD_ID);

	// the void totem item
	public static final RegistryObject<Item> VOID_TOTEM_ITEM = ITEMS.register(ModConstants.ITEM_VOID_TOTEM,
			() -> new Item(new Item.Properties().maxStackSize(1).group(ItemGroup.COMBAT).rarity(Rarity.UNCOMMON)));

	@SubscribeEvent
	public void loadLootTables(final LootTableLoadEvent event) {
		if (VoidTotemConfig.COMMON_CONFIG.ADD_END_CITY_TREASURE.get()) {

			if (event.getName().equals(ModConstants.LOCATION_END_CITY_TREASURE)) {
				LOGGER.debug("Injecting loottable {} from {}", ModConstants.LOCATION_END_CITY_TREASURE.toString(),
						ModConstants.MOD_ID);
				event.getTable()
						.addPool(LootPool.builder()
								.addEntry(TableLootEntry.builder(ModConstants.LOCATION_END_CITY_TREASURE_INJECTION))
								.name(ModConstants.MOD_ID + "_injection").build());
			}
		}
	}

	/**
	 * Used to send an imc message to the curios api if the mod is loaded.
	 * 
	 * @param event InterModEnqueueEvent
	 */
	private void enqueueIMC(final InterModEnqueueEvent event) {
		if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) {
			InterModComms.sendTo(ModConstants.CURIOS_MOD_ID, SlotTypeMessage.REGISTER_TYPE,
					() -> SlotTypePreset.CHARM.getMessageBuilder().build());
			VoidTotem.LOGGER.debug("Enqueued IMC to {}", ModConstants.CURIOS_MOD_ID);
		}
	}

	/**
	 * Used to create the json for lang / item / recipes / tags.
	 * 
	 * @param event GatherDataEvent
	 */
	private void gatherData(final GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		if (event.includeServer()) { // server side generators
			generator.addProvider(new ModDataGeneration.RecipeGen(generator));
			BlockTagsProvider blockTagsProvider = new ModDataGeneration.BlockTagsGen(generator, ModConstants.MOD_ID,
					existingFileHelper);
			generator.addProvider(new ModDataGeneration.ItemTagsGen(generator, blockTagsProvider, ModConstants.MOD_ID,
					existingFileHelper));
			generator.addProvider(new ModDataGeneration.AdvancementGen(generator));
			generator.addProvider(new ModDataGeneration.LootTableGen(generator));
		}
		if (event.includeClient()) { // client side generators
			generator.addProvider(new ModDataGeneration.LanguageGen(generator, ModConstants.MOD_ID, "de_de"));
			generator.addProvider(new ModDataGeneration.LanguageGen(generator, ModConstants.MOD_ID, "en_us"));
			generator.addProvider(
					new ModDataGeneration.ItemModelGen(generator, ModConstants.MOD_ID, existingFileHelper));
		}
	}

	/**
	 * Used to add a tooltip for the void totem item.
	 * 
	 * @param event ItemTooltipEvent
	 */
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void tooltip(final ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (stack.getItem() == VoidTotem.VOID_TOTEM_ITEM.get()
				&& VoidTotemConfig.COMMON_CONFIG.SHOW_TOTEM_TOOLTIP.get()) {
			event.getToolTip().add(
					new TranslationTextComponent(ModConstants.TOOLTIP_VOID_TOTEM).mergeStyle(TextFormatting.GREEN));
		}
	}

	/**
	 * Used to check whether the player has the tag to prevent vanilla flying kick
	 * and to check if the player is still flying and the removing the tag.
	 * 
	 * @param event PlayerTickEvent
	 */
	private void playerTick(final PlayerTickEvent event) {
		if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.START) {
			if (event.player instanceof ServerPlayerEntity) {
				ServerPlayerEntity player = (ServerPlayerEntity) event.player;
				if (player.getPersistentData().getBoolean(ModConstants.NBT_TAG)) {
					player.connection.floatingTickCount = 0;
					if (player.isInWater() || player.abilities.isFlying || player.abilities.allowFlying
							|| player.world.getBlockState(player.getPosition()).getBlock() == Blocks.COBWEB) {
						player.getPersistentData().putBoolean(ModConstants.NBT_TAG, false);
					}
				}
			}
		}
	}

	/**
	 * Used to do the void totem mechanic.
	 * 
	 * How it works in general:
	 * 
	 * 1. Check if it is on server side.
	 * 
	 * 2. Check if the current world isn't blacklisted.
	 * 
	 * 3. Check if the damage source is DamageSource.OUT_OF_WORLD.
	 * 
	 * 4. Check if the entity is below y = -64.
	 * 
	 * 5. Check if the damage is higher than actual health.
	 * 
	 * 6. Check if entity is a server player entity.
	 * 
	 * 7. Check a totem is needed (VoidTotemConfig#NEEDS_TOTEM).
	 * 
	 * 8. Else check if totems will be used from your inventory
	 * (VoidTotemConfig#USE_TOTEM_FROM_INVENTORY).
	 * 
	 * 9. Else check if curios api is installed and take the totem from the charm
	 * slot.
	 * 
	 * 10. Else check for a totem in the main / offhand.
	 * 
	 * 11. Check if there is a totem anywhere (see 7. - 10.).
	 * 
	 * 12. Prepare for teleporting.
	 * 
	 * 13. Try 16 times to teleport the player to block below.
	 * 
	 * 14. Else just teleport the player to the height set in the config
	 * (VoidTotemConfig#TELEPORT_HEIGHT).
	 * 
	 * 15. Cancle the event, and give the player the tag.
	 * 
	 * 16. Send the TotemEffectPacket to the player and the players around.
	 * 
	 * @param event LivingHurtEvent
	 */
	private void livingHurt(final LivingHurtEvent event) {
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
		if (event.getAmount() < event.getEntityLiving().getHealth()) // only run if player about to die
			return;
		if (event.getEntityLiving() instanceof ServerPlayerEntity) { // is server player entity
			ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
			player.connection.floatingTickCount = 0;

			ItemStack itemstack = null;
			boolean foundValidStack = false;

			if (!VoidTotemConfig.COMMON_CONFIG.NEEDS_TOTEM.get()) { // totem is needed (config)
				itemstack = ItemStack.EMPTY;
				foundValidStack = true;
			} else if (VoidTotemConfig.COMMON_CONFIG.USE_TOTEM_FROM_INVENTORY.get()) { // totems in the player inv used
																						// (config)
				for (int i = 0; i < player.inventory.getSizeInventory(); i++) { // for each player inventory slot
					ItemStack stack = player.inventory.getStackInSlot(i);
					if (isVoidTotemOrTotem(stack)) { // is valid item
						itemstack = copyAndRemoveItemStack(stack, player);
						foundValidStack = true;
						break;
					}
				}
			} else {
				if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) { // curios api is loaded
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
					for (Hand hand : Hand.values()) { // for each hand (main-/offhand)
						ItemStack stack = player.getHeldItem(hand);
						if (isVoidTotemOrTotem(stack)) { // is valid item
							itemstack = copyAndRemoveItemStack(stack, player);
							foundValidStack = true;
							break;
						}
					}
				}
			}

			if (itemstack != null && foundValidStack) { // check if stack isn't null and if there
				if (player.connection.targetPos != null) // wants to teleport
					return;
				if (player.isBeingRidden()) { // has passenger and remove it
					player.removePassengers();
				}
				player.stopRiding();

				boolean teleportedToBlock = false;
				for (int i = 0; i < 16; i++) { // try 16 times to teleport the player to a good spot
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

	/**
	 * Used to check if the given stack is valid.
	 * 
	 * @param stack ItemStack
	 * @return true if the given stack has a void totem or a totem of undying (has
	 *         to be enabled in the config).
	 */
	public static boolean isVoidTotemOrTotem(ItemStack stack) {
		Item item = stack.getItem();
		boolean isVoidTotem = item == VoidTotem.VOID_TOTEM_ITEM.get();
		boolean isTotemOfUndying = VoidTotemConfig.COMMON_CONFIG.ALLOW_TOTEM_OF_UNDYING.get()
				? item == Items.TOTEM_OF_UNDYING
				: false;
		if (isVoidTotem || isTotemOfUndying) {
			return true;
		}
		return false;
	}

	/**
	 * Used to copy a given ItemStack of a ServerPlayerEntity, add an ITEM_USED
	 * stat, grant the void totem advancement, shrink the stack and and return the
	 * copied stack.
	 * 
	 * @param itemStack ItemStack
	 * @param player    ServerPlayerEntity
	 * @return a copy of the given ItemStack.
	 */
	private static ItemStack copyAndRemoveItemStack(ItemStack itemStack, ServerPlayerEntity player) {
		ItemStack itemStackCopy = itemStack.copy();
		if (!itemStack.isEmpty()) { // add stats if stack isn't empty / null
			player.addStat(Stats.ITEM_USED.get(itemStack.getItem()));
			CriteriaTriggers.USED_TOTEM.trigger(player, itemStack);
		}
		itemStack.shrink(1);
		return itemStackCopy;
	}

	/**
	 * Used to prevent fall damage when the player has the tag.
	 * 
	 * @param event LivingFallEvent
	 */
	private void livingFall(final LivingFallEvent event) {
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

	/**
	 * Used to play the totem animation for the given entity.
	 * 
	 * @param stack  ItemStack
	 * @param entity Entity
	 */
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
}