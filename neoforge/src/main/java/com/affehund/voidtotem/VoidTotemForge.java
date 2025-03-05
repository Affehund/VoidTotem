package com.affehund.voidtotem;


import com.affehund.voidtotem.config.VoidTotemCommonConfig;
import com.affehund.voidtotem.core.ModUtils;
import com.affehund.voidtotem.data.VoidTotemDataGeneration;
import com.affehund.voidtotem.integration.CuriosCombatHandler;
import com.affehund.voidtotem.network.TotemEffectPacket;
import com.affehund.voidtotem.network.VoidTotemPayloadHandler;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

@Mod(ModConstants.MOD_ID)
public class VoidTotemForge {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModConstants.MOD_ID);
    public static final DeferredItem<Item> VOID_TOTEM_ITEM = ITEMS.registerSimpleItem(ModConstants.ITEM_VOID_TOTEM, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, ModConstants.MOD_ID);
    public static final Supplier<SimpleParticleType> VOID_TOTEM_PARTICLE = PARTICLE_TYPES.register("void_totem", () -> new SimpleParticleType(false));

    public VoidTotemForge(IEventBus eventBus, ModContainer modContainer) {
        eventBus.addListener(this::addCreative);
        eventBus.addListener(this::gatherData);
        eventBus.addListener(this::registerPayloadHandler);

        if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) {
            eventBus.addListener(CuriosCombatHandler::registerCapabilities);
        }

        ITEMS.register(eventBus);
        PARTICLE_TYPES.register(eventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, VoidTotemCommonConfig.SPEC);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertAfter(new ItemStack(Items.TOTEM_OF_UNDYING), new ItemStack(VOID_TOTEM_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();
        var isClientProvider = event.includeClient();
        var isServerProvider = event.includeServer();
        var blockTagsGen = new VoidTotemDataGeneration.BlockTagsGen(packOutput, lookupProvider, existingFileHelper);

        //  Server Side generators
        generator.addProvider(isServerProvider, new AdvancementProvider(packOutput, lookupProvider, existingFileHelper, List.of(new VoidTotemDataGeneration.AdvancementGen())));
        generator.addProvider(isServerProvider, blockTagsGen);
        generator.addProvider(isServerProvider, new VoidTotemDataGeneration.RecipeGen(packOutput, lookupProvider));

        //  Client Side generators
        generator.addProvider(isClientProvider, new VoidTotemDataGeneration.LanguageGen(packOutput, "de_de"));
        generator.addProvider(isClientProvider, new VoidTotemDataGeneration.LanguageGen(packOutput, "en_us"));
        generator.addProvider(isClientProvider, new VoidTotemDataGeneration.ItemModelGen(packOutput, existingFileHelper));
    }

    private void registerPayloadHandler(final RegisterPayloadHandlersEvent evt) {
        evt.registrar(ModConstants.MOD_ID)
                .playToClient(TotemEffectPacket.TYPE, TotemEffectPacket.STREAM_CODEC,
                        VoidTotemPayloadHandler.getInstance()::handleUseTotem);
    }
}