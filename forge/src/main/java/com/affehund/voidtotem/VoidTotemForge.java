package com.affehund.voidtotem;

import com.affehund.voidtotem.client.VoidTotemCuriosRenderer;
import com.affehund.voidtotem.core.*;
import com.affehund.voidtotem.core.network.PacketHandler;
import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
import java.util.Collections;
import java.util.List;

@Mod(ModConstants.MOD_ID)
public class VoidTotemForge {

    public VoidTotemForge() {
        VoidTotem.init();

        if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) {
            InterModComms.sendTo(ModConstants.CURIOS_MOD_ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.CHARM.getMessageBuilder().build());
        }

        var forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.addGenericListener(ItemStack.class, this::attachCaps);
        forgeEventBus.addListener(this::itemTooltip);
        forgeEventBus.register(this);

        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::buildContents);
        ITEMS.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);

        GLOBAL_LOOT_MODIFIERS.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, VoidTotemCommonConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, VoidTotemClientConfig.SPEC);

        PacketHandler.registerMessages();
    }

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModConstants.MOD_ID);
    public static final RegistryObject<Item> VOID_TOTEM_ITEM = ITEMS.register(ModConstants.ITEM_VOID_TOTEM, () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ModConstants.MOD_ID);
    public static final RegistryObject<SimpleParticleType> VOID_TOTEM_PARTICLE =
            PARTICLE_TYPES.register("void_totem", () -> new SimpleParticleType(true));

    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ModConstants.MOD_ID);
    public static final RegistryObject<Codec<EndCityTreasureModifier>> END_CITY_TREASURE_LOOT = GLOBAL_LOOT_MODIFIERS.register("end_city_treasure_loot_modifier", EndCityTreasureModifier.CODEC);


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

    private void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.getEntries().putAfter(new ItemStack(Items.TOTEM_OF_UNDYING), new ItemStack(VOID_TOTEM_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private void clientSetup(FMLClientSetupEvent event) {
        if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID) && VoidTotemClientConfig.DISPLAY_TOTEM_ON_CHEST.get()) {
            CuriosRendererRegistry.register(VOID_TOTEM_ITEM.get(), VoidTotemCuriosRenderer::new);
        }
    }

    private void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        var lookup = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();
        var isClientProvider = event.includeClient();
        var isServerProvider = event.includeServer();
        var blockTagsGen = new VoidTotemDataGeneration.BlockTagsGen(packOutput, lookup, existingFileHelper);

        //  Server Side generators
        generator.addProvider(isServerProvider, new VoidTotemDataGeneration.ItemTagsGen(packOutput, lookup, blockTagsGen, existingFileHelper));
        generator.addProvider(isServerProvider, new ForgeAdvancementProvider(packOutput, lookup, existingFileHelper, List.of(new VoidTotemDataGeneration.AdvancementGen())));
        generator.addProvider(isServerProvider, new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(VoidTotemDataGeneration.LootTableGen::new, LootContextParamSets.CHEST))));
        generator.addProvider(isServerProvider, new VoidTotemDataGeneration.RecipeGen(packOutput));

        //  Client Side generators
        generator.addProvider(isClientProvider, new VoidTotemDataGeneration.LanguageGen(packOutput, "de_de"));
        generator.addProvider(isClientProvider, new VoidTotemDataGeneration.LanguageGen(packOutput, "en_us"));
        generator.addProvider(isClientProvider, new VoidTotemDataGeneration.ItemModelGen(packOutput, existingFileHelper));
    }

    private void itemTooltip(ItemTooltipEvent event) {
        VoidTotem.onItemTooltip(event.getItemStack(), event.getToolTip());
    }
}