package com.affehund.voidtotem;

import com.affehund.voidtotem.core.ModDataGeneration;
import com.affehund.voidtotem.core.ModUtils;
import com.affehund.voidtotem.core.VoidTotemConfig;
import com.affehund.voidtotem.core.network.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
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
import top.theillusivec4.curios.api.SlotContext;
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
            InterModComms.sendTo(ModConstants.CURIOS_MOD_ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.CHARM.getMessageBuilder().build());
            VoidTotem.LOGGER.debug("Enqueued IMC to {}", ModConstants.CURIOS_MOD_ID);
        }

        modEventBus.addListener(this::gatherData);
        ITEMS.register(modEventBus);

        forgeEventBus.register(this);
        forgeEventBus.addListener(this::livingHurt);
        forgeEventBus.addListener(this::livingFall);
        forgeEventBus.addListener(this::worldTick);
        forgeEventBus.addGenericListener(ItemStack.class, this::attachCaps);

        PacketHandler.registerMessages();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, VoidTotemConfig.COMMON_CONFIG_SPEC,
                ModConstants.COMMON_CONFIG_NAME);

        LOGGER.debug("{} has finished loading for now!", ModConstants.MOD_NAME);
    }

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModConstants.MOD_ID);
    public static final RegistryObject<Item> VOID_TOTEM_ITEM = ITEMS.register(ModConstants.ITEM_VOID_TOTEM, () -> new Item(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON)));

    private void attachCaps(AttachCapabilitiesEvent<ItemStack> event) {
        if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) {
            var stack = event.getObject();
            if (ModUtils.isVoidTotemOrAdditionalTotem(stack)) {
                event.addCapability(new ResourceLocation(ModConstants.MOD_ID, ModConstants.CURIOS_MOD_ID), new ICapabilityProvider() {
                    final ICurio curio = new ICurio() {
                        @Override
                        public boolean canEquipFromUse(SlotContext slotContext) {
                            return true;
                        }

                        @Override
                        public ItemStack getStack() {
                            return stack;
                        }
                    };

                    @Nonnull
                    @Override
                    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
                        return CuriosCapability.ITEM.orEmpty(capability, LazyOptional.of(() -> curio));
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void loadLootTables(LootTableLoadEvent event) {
        if (VoidTotemConfig.COMMON_CONFIG.ADD_END_CITY_TREASURE.get()) {
            if (event.getName().equals(ModConstants.LOCATION_END_CITY_TREASURE)) {
                LOGGER.debug("Injecting loot table {} from {}", ModConstants.LOCATION_END_CITY_TREASURE.toString(),
                        ModConstants.MOD_ID);
                event.getTable().addPool(LootPool.lootPool()
                        .add(LootTableReference.lootTableReference(ModConstants.LOCATION_END_CITY_TREASURE_INJECTION))
                        .name(ModConstants.MOD_ID + "_injection").build());
            }
        }
    }

    private void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var existingFileHelper = event.getExistingFileHelper();

        if (event.includeServer()) {
            generator.addProvider(new ModDataGeneration.AdvancementGen(generator, existingFileHelper));
            var blockTagsProvider = new ModDataGeneration.BlockTagsGen(generator, ModConstants.MOD_ID, existingFileHelper);
            generator.addProvider(new ModDataGeneration.ItemTagsGen(generator, blockTagsProvider, ModConstants.MOD_ID, existingFileHelper));
            generator.addProvider(new ModDataGeneration.LootTableGen(generator));
            generator.addProvider(new ModDataGeneration.RecipeGen(generator));
        }

        if (event.includeClient()) {
            generator.addProvider(new ModDataGeneration.LanguageGen(generator, "de_de"));
            generator.addProvider(new ModDataGeneration.LanguageGen(generator, "en_us"));
            generator.addProvider(new ModDataGeneration.ItemModelGen(generator, ModConstants.MOD_ID, existingFileHelper));
        }
    }

    private void livingFall(LivingFallEvent event) {
        //if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getEntity().getPersistentData().getBoolean(ModConstants.NBT_TAG)) {
                if (event.getEntity() instanceof ServerPlayer player)
                player.connection.aboveGroundTickCount = 0;
                event.setDamageMultiplier(0f);
                if (event.getEntity() instanceof ServerPlayer player)
                player.getPersistentData().putBoolean(ModConstants.NBT_TAG, false);
                event.setCanceled(true);
            }
       // }
    }

    private void worldTick(TickEvent.WorldTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.START && event.world instanceof ServerLevel level) {
            for (Entity entity : level.getEntities().getAll()) {
                BlockPos pos = entity.blockPosition();

                long lastPosLong = entity.getPersistentData().getLong(ModConstants.LAST_BLOCK_POS);
                BlockPos lastPos = BlockPos.of(lastPosLong);
                if (entity.level.getBlockState(pos.below()).canOcclude()) {
                    if (!lastPos.equals(pos)) {
                        entity.getPersistentData().putLong(ModConstants.LAST_BLOCK_POS, pos.asLong());
                    }
                }

                if (entity.getPersistentData().getBoolean(ModConstants.NBT_TAG)) {
                    if (entity instanceof ServerPlayer player)
                    player.connection.aboveGroundTickCount = 0;
                    if (entity.isInWater() || entity instanceof ServerPlayer player && (player.getAbilities().flying || player.getAbilities().mayfly)
                            || entity.level.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        entity.getPersistentData().putBoolean(ModConstants.NBT_TAG, false);
                    }
                }
            }
        }
    }

    private void livingHurt(LivingHurtEvent event) {
        var livingEntity = event.getEntityLiving();
        if (event.getAmount() >= livingEntity.getHealth() && ModUtils.tryUseVoidTotem(livingEntity, event.getSource())) {
            event.setCanceled(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void tooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        if (ModUtils.isVoidTotemOrAdditionalTotem(stack) && VoidTotemConfig.COMMON_CONFIG.SHOW_TOTEM_TOOLTIP.get()) {
            event.getToolTip().add(new TranslatableComponent(ModConstants.TOOLTIP_VOID_TOTEM).withStyle(ChatFormatting.GREEN));
        }
    }
}
