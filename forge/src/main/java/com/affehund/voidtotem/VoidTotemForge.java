package com.affehund.voidtotem;

import com.affehund.voidtotem.client.VoidTotemCuriosRenderer;
import com.affehund.voidtotem.core.ModUtils;
import com.affehund.voidtotem.core.VoidTotemConfig;
import com.affehund.voidtotem.core.VoidTotemDataGeneration;
import com.affehund.voidtotem.core.network.PacketHandler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod(ModConstants.MOD_ID)
public class VoidTotemForge {

    public VoidTotemForge() {
        VoidTotem.init();

        if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) {
            InterModComms.sendTo(ModConstants.CURIOS_MOD_ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.CHARM.getMessageBuilder().build());
            VoidTotem.LOGGER.debug("Enqueued IMC to {}", ModConstants.CURIOS_MOD_ID);
        }

        var forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.addGenericListener(ItemStack.class, this::attachCaps);
        forgeEventBus.addListener(this::itemTooltip);
        forgeEventBus.register(this);

        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::clientSetup);
        ITEMS.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, VoidTotemConfig.COMMON_SPEC);

        PacketHandler.registerMessages();
    }

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModConstants.MOD_ID);
    public static final RegistryObject<Item> VOID_TOTEM_ITEM = ITEMS.register(ModConstants.ITEM_VOID_TOTEM, () -> new Item(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON)));

    private void clientSetup(FMLClientSetupEvent event) {
        if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID) && VoidTotemConfig.DISPLAY_TOTEM_ON_CHEST.get()) {
            CuriosRendererRegistry.register(VOID_TOTEM_ITEM.get(), VoidTotemCuriosRenderer::new);
        }
    }

    private void attachCaps(AttachCapabilitiesEvent<ItemStack> event) {
        if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) {
            var itemStack = event.getObject();
            var item = itemStack.getItem();
            if (item.equals(VOID_TOTEM_ITEM.get())) {
                event.addCapability(new ResourceLocation(ModConstants.MOD_ID, ModConstants.CURIOS_MOD_ID), new ICapabilityProvider() {
                    final ICurio curio = new ICurio() {
                        @Override
                        public boolean canEquipFromUse(SlotContext slotContext) {
                            return true;
                        }

                        @Override
                        public ItemStack getStack() {
                            return itemStack;
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

    private void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var existingFileHelper = event.getExistingFileHelper();
        var isClientProvider = event.includeClient();

        //  Server Side generators
        var blockTagsProvider = new VoidTotemDataGeneration.BlockTagsGen(generator, ModConstants.MOD_ID, existingFileHelper);
        generator.addProvider(isClientProvider, new VoidTotemDataGeneration.ItemTagsGen(generator, blockTagsProvider, ModConstants.MOD_ID, existingFileHelper));
        generator.addProvider(isClientProvider, new VoidTotemDataGeneration.AdvancementGen(generator, existingFileHelper));
        generator.addProvider(isClientProvider, new VoidTotemDataGeneration.LootTableGen(generator));
        generator.addProvider(isClientProvider, new VoidTotemDataGeneration.RecipeGen(generator));

        //  Client Side generators
        generator.addProvider(isClientProvider, new VoidTotemDataGeneration.LanguageGen(generator, "de_de"));
        generator.addProvider(isClientProvider, new VoidTotemDataGeneration.LanguageGen(generator, "en_us"));
        generator.addProvider(isClientProvider, new VoidTotemDataGeneration.ItemModelGen(generator, ModConstants.MOD_ID, existingFileHelper));
    }

    @SubscribeEvent
    public void loadLootTables(LootTableLoadEvent event) {
        if (VoidTotemConfig.ADD_END_CITY_TREASURE.get() && event.getName().equals(BuiltInLootTables.END_CITY_TREASURE)) {
            event.getTable().addPool(LootPool.lootPool()
                    .add(LootTableReference.lootTableReference(ModConstants.END_CITY_TREASURE_INJECTION_LOCATION))
                    .name(ModConstants.MOD_ID + "_injection").build());
        }
    }

    private void itemTooltip(ItemTooltipEvent event) {
        VoidTotem.onItemTooltip(event.getItemStack(), event.getToolTip());
    }
}