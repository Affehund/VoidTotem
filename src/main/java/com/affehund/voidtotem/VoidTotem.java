package com.affehund.voidtotem;

import com.affehund.voidtotem.core.ModDataGeneration;
import com.affehund.voidtotem.core.ModUtils;
import com.affehund.voidtotem.core.VoidTotemConfig;
import com.affehund.voidtotem.core.network.PacketHandler;
import com.affehund.voidtotem.core.network.TotemEffectPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod(ModConstants.MOD_ID)
public class VoidTotem {

    public static final Logger LOGGER = LogManager.getLogger(ModConstants.MOD_NAME);

    public static VoidTotem INSTANCE;

    final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    final IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

    public VoidTotem() {
        INSTANCE = this;
        LOGGER.debug("Loading up {}!", ModConstants.MOD_NAME);

        if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) {
            InterModComms.sendTo(ModConstants.CURIOS_MOD_ID, SlotTypeMessage.REGISTER_TYPE,
                    () -> SlotTypePreset.CHARM.getMessageBuilder().build());
            VoidTotem.LOGGER.debug("Enqueued IMC to {}", ModConstants.CURIOS_MOD_ID);
        }

        modEventBus.addListener(this::gatherData);
        ITEMS.register(modEventBus);

        forgeEventBus.register(this);
        forgeEventBus.addListener(this::livingHurt);
        forgeEventBus.addListener(this::livingFall);
        forgeEventBus.addListener(this::playerTick);

        forgeEventBus.addGenericListener(ItemStack.class, this::attachCaps);

        // register of the messages
        PacketHandler.registerMessages();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, VoidTotemConfig.COMMON_CONFIG_SPEC,
                ModConstants.COMMON_CONFIG_NAME);
    }

    // deferred register
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            ModConstants.MOD_ID);

    // the void totem item
    public static final RegistryObject<Item> VOID_TOTEM_ITEM = ITEMS.register(ModConstants.ITEM_VOID_TOTEM,
            () -> new Item(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON)));

    private void attachCaps(AttachCapabilitiesEvent<ItemStack> event) {
        if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) {
            ItemStack stack = event.getObject();
            Item item = stack.getItem();
            if (item.getRegistryName() != null && isVoidTotemOrTotem(stack)) {
                VoidTotem.LOGGER.debug("Attached Curios Capability to {}", item.getRegistryName());
                event.addCapability(new ResourceLocation(ModConstants.MOD_ID, item.getRegistryName().getPath() + "_curios"), new ICapabilityProvider() {
                    final ICurio curio = new ICurio() {
                        @Override
                        public boolean canRightClickEquip() {
                            return true;
                        }

                        @Override
                        public ItemStack getStack() {
                            return stack;
                        }
                    };

                    @Nonnull
                    @Override
                    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                        return CuriosCapability.ITEM.orEmpty(cap, LazyOptional.of(() -> curio));
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void loadLootTables(final LootTableLoadEvent event) {
        if (VoidTotemConfig.COMMON_CONFIG.ADD_END_CITY_TREASURE.get()) {
            if (event.getName().equals(ModConstants.LOCATION_END_CITY_TREASURE)) {
                LOGGER.debug("Injecting loottable {} from {}", ModConstants.LOCATION_END_CITY_TREASURE.toString(),
                        ModConstants.MOD_ID);
                event.getTable().addPool(LootPool.lootPool()
                        .add(LootTableReference.lootTableReference(ModConstants.LOCATION_END_CITY_TREASURE_INJECTION))
                        .name(ModConstants.MOD_ID + "_injection").build());
            }
        }
    }

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
            generator.addProvider(new ModDataGeneration.LanguageGen(generator, "de_de"));
            generator.addProvider(new ModDataGeneration.LanguageGen(generator, "en_us"));
            generator.addProvider(new ModDataGeneration.LanguageGen(generator, "ru_ru"));
            generator.addProvider(
                    new ModDataGeneration.ItemModelGen(generator, ModConstants.MOD_ID, existingFileHelper));
        }
    }

    private void livingFall(final LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getPersistentData().getBoolean(ModConstants.NBT_TAG)) { // if player has tag
                player.connection.aboveGroundTickCount = 0;
                event.setDamageMultiplier(0f); // set damage multiplier to 0 => no damage
                player.getPersistentData().putBoolean(ModConstants.NBT_TAG, false); // remove tag
                event.setCanceled(true);
            }
        }
    }

    private void playerTick(final TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.START) {
            if (event.player instanceof ServerPlayer player) {

                BlockPos pos = player.blockPosition();

                long lastPosLong = player.getPersistentData().getLong(ModConstants.LAST_BLOCK_POS);
                BlockPos lastPos = BlockPos.of(lastPosLong);
                if (player.level.getBlockState(pos.below()).canOcclude()) {
                    if (!lastPos.equals(pos)) {
                        player.getPersistentData().putLong(ModConstants.LAST_BLOCK_POS, pos.asLong());
                    }
                }

                if (player.getPersistentData().getBoolean(ModConstants.NBT_TAG)) {
                    player.connection.aboveGroundTickCount = 0;
                    if (player.isInWater() || player.getAbilities().flying || player.getAbilities().mayfly
                            || player.level.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        player.getPersistentData().putBoolean(ModConstants.NBT_TAG, false);
                    }
                }
            }
        }
    }

    private void livingHurt(final LivingHurtEvent event) {
        Entity entity = event.getEntity();
        if (entity.level.isClientSide()) return;
        if (VoidTotemConfig.COMMON_CONFIG.BLACKLISTED_DIMENSIONS.get().contains(entity.level.dimension().location().toString())) return;
        if (event.getSource() != DamageSource.OUT_OF_WORLD || entity.getY() > -64) return;
        if (event.getAmount() < event.getEntityLiving().getHealth()) return;

        if (event.getEntityLiving() instanceof ServerPlayer player) { // is server player entity
            player.connection.aboveGroundTickCount = 0;

            ItemStack itemstack = null;

            if (!VoidTotemConfig.COMMON_CONFIG.NEEDS_TOTEM.get()) { // totem is needed (config)
                itemstack = ItemStack.EMPTY;
            }

            if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID) && itemstack == null) { // curios api is loaded
                ItemStack curiosVoidTotemStack = ModUtils.findCuriosItem(VoidTotem.VOID_TOTEM_ITEM.get(), player);
                ItemStack curiosVanillaTotemStack = VoidTotemConfig.COMMON_CONFIG.ALLOW_TOTEM_OF_UNDYING.get()
                        ? ModUtils.findCuriosItem(Items.TOTEM_OF_UNDYING, player)
                        : ItemStack.EMPTY;
                if (!curiosVoidTotemStack.isEmpty()) {
                    itemstack = copyAndRemoveItemStack(curiosVoidTotemStack, player);
                } else if (!curiosVanillaTotemStack.isEmpty()) {
                    itemstack = copyAndRemoveItemStack(curiosVanillaTotemStack, player);
                }
            }

            if (VoidTotemConfig.COMMON_CONFIG.USE_TOTEM_FROM_INVENTORY.get() && itemstack == null) { // totems in the player inv used (config)
                for (ItemStack itemStack : player.getInventory().items) { // for each player inventory slot
                    if (isVoidTotemOrTotem(itemStack)) { // is valid item
                        itemstack = copyAndRemoveItemStack(itemStack, player);
                        break;
                    }
                }
            }

            if (itemstack == null) {
                for (InteractionHand hand : InteractionHand.values()) { // for each hand (main-/offhand)
                    ItemStack stack = player.getItemInHand(hand);
                    if (isVoidTotemOrTotem(stack)) { // is valid item
                        itemstack = copyAndRemoveItemStack(stack, player);
                        break;
                    }
                }
            }

            if (itemstack != null) { // check if stack isn't null and if there
                if (player.connection.awaitingPositionFromClient != null) // wants to teleport
                    return;
                if (player.isVehicle()) { // has passenger and remove it
                    player.ejectPassengers();
                }
                player.stopRiding();

                long lastBlockPos = player.getPersistentData().getLong(ModConstants.LAST_BLOCK_POS); // get last pos
                BlockPos teleportPos = BlockPos.of(lastBlockPos); // convert to block pos

                boolean teleportedToBlock = false;

                for (int i = 0; i < 16; i++) { // try 16 times to teleport the player to a good spot

                    double x = teleportPos.getX() + (player.getRandom().nextDouble() - 0.5D) * 4.0D;
                    double y = Mth.clamp(player.getRandom().nextInt() * player.level.getHeight(), 0.0D,
                            player.level.getHeight() - 1);
                    double z = teleportPos.getZ() + (player.getRandom().nextDouble() - 0.5D) * 4.0;

                    if (player.randomTeleport(x, y, z, true)) { // if player can teleport break
                        teleportedToBlock = true;
                        break;
                    }
                }

                if (!teleportedToBlock) { // if player can't teleport to a block teleport to height set in config
                    player.teleportTo(teleportPos.getX(), VoidTotemConfig.COMMON_CONFIG.TELEPORT_HEIGHT.get(),
                            teleportPos.getZ());
                    player.connection.aboveGroundTickCount = 0;
                }

                event.setCanceled(true);
                player.getPersistentData().putBoolean(ModConstants.NBT_TAG, true); // add tag to prevent fall damage

                PacketHandler.sendToPlayer(new TotemEffectPacket(itemstack, player), player); // totem effect packet
                PacketHandler.sendToAllTracking(new TotemEffectPacket(itemstack, player), player); // to all tracking
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void tooltip(final ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (isVoidTotemOrTotem(stack)
                && VoidTotemConfig.COMMON_CONFIG.SHOW_TOTEM_TOOLTIP.get()) {
            event.getToolTip()
                    .add(new TranslatableComponent(ModConstants.TOOLTIP_VOID_TOTEM).withStyle(ChatFormatting.GREEN));
        }
    }

    public static boolean isVoidTotemOrTotem(ItemStack stack) {
        Item item = stack.getItem();
        boolean isVoidTotem = item == VoidTotem.VOID_TOTEM_ITEM.get();
        boolean isTotemOfUndying = VoidTotemConfig.COMMON_CONFIG.ALLOW_TOTEM_OF_UNDYING.get() && item == Items.TOTEM_OF_UNDYING;
        return isVoidTotem || isTotemOfUndying;
    }

    private static ItemStack copyAndRemoveItemStack(ItemStack itemStack, ServerPlayer player) {
        ItemStack itemStackCopy = itemStack.copy();
        if (!itemStack.isEmpty()) { // add stats if stack isn't empty / null
            player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
            CriteriaTriggers.USED_TOTEM.trigger(player, itemStack);
        }
        itemStack.shrink(1);
        return itemStackCopy;
    }

    @OnlyIn(Dist.CLIENT)
    public void playActivateAnimation(ItemStack stack, Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        mc.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30); // particles
        assert mc.level != null;
        mc.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE,
                entity.getSoundSource(), 1.0F, 1.0F, false); // sound

        if (entity == mc.player) {
            mc.gameRenderer.displayItemActivation(stack); // animation
        }
    }
}
