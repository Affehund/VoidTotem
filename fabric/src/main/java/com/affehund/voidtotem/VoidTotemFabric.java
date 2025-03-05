package com.affehund.voidtotem;

import com.affehund.voidtotem.config.VoidTotemAutoConfig;
import com.affehund.voidtotem.network.TotemEffectPacket;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;

public class VoidTotemFabric implements ModInitializer {

    public static final Item VOID_TOTEM_ITEM = new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final SimpleParticleType VOID_TOTEM_PARTICLE = FabricParticleTypes.simple();

    public static VoidTotemAutoConfig CONFIG;

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(TotemEffectPacket.TYPE, TotemEffectPacket.STREAM_CODEC);

        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, ModConstants.ITEM_VOID_TOTEM), VOID_TOTEM_ITEM);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "void_totem"), VOID_TOTEM_PARTICLE);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> entries.addAfter(new ItemStack(Items.TOTEM_OF_UNDYING), VOID_TOTEM_ITEM));

        AutoConfig.register(VoidTotemAutoConfig.class, Toml4jConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(VoidTotemAutoConfig.class).getConfig();

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin() && BuiltInLootTables.END_CITY_TREASURE.equals(key)) {
                LootPool.Builder poolBuilder = LootPool.lootPool().add(LootItem.lootTableItem(VOID_TOTEM_ITEM)).setRolls(BinomialDistributionGenerator.binomial(1, 0.25F));
                tableBuilder.withPool(poolBuilder);
            }
        });
    }
}
