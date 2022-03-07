package com.affehund.voidtotem;

import com.affehund.voidtotem.core.ModConstants;
import com.affehund.voidtotem.core.config.VoidTotemConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplier;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.loot.LootTable;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VoidTotem implements ModInitializer {
    public static final Item VOID_TOTEM_ITEM = new Item(
            new Item.Settings().maxCount(1).group(ItemGroup.COMBAT).rarity(Rarity.UNCOMMON));

    public static final Logger LOGGER = LogManager.getLogger(ModConstants.MOD_NAME);

    public static VoidTotemConfig CONFIG;

    @Override
    public void onInitialize() {
        LOGGER.debug("Loading up {}!", ModConstants.MOD_NAME);
        Registry.register(Registry.ITEM, new Identifier(ModConstants.MOD_ID, ModConstants.ITEM_VOID_TOTEM),
                VOID_TOTEM_ITEM);

        AutoConfig.register(VoidTotemConfig.class, Toml4jConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(VoidTotemConfig.class).getConfig();

        LootTableLoadingCallback.EVENT.register((resourceManager, lootManager, id, supplier, setter) -> {
            if (ModConstants.IDENTIFIER_END_CITY_TREASURE.equals(id) && CONFIG.ADD_END_CITY_TREASURE) {
                LootTable table = lootManager.getTable(ModConstants.IDENTIFIER_END_CITY_TREASURE_INJECTION);
                supplier.withPools(((FabricLootSupplier) table).getPools());
            }
        });
    }
}
