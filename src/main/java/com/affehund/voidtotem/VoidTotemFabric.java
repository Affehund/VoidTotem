package com.affehund.voidtotem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.affehund.voidtotem.core.VoidTotemConfig;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeInfo.BuildScheme;
import top.theillusivec4.curios.api.SlotTypePreset;

public class VoidTotemFabric implements ModInitializer {
	public static final Item VOID_TOTEM_ITEM = new Item(
			new Item.Settings().maxCount(1).group(ItemGroup.COMBAT).rarity(Rarity.UNCOMMON));

	public static final Logger LOGGER = LogManager.getLogger(ModConstants.MOD_NAME);

	public static VoidTotemConfig CONFIG;

	@Override
	public void onInitialize() {
		LOGGER.debug("Loading up {}!", ModConstants.MOD_NAME);
		Registry.register(Registry.ITEM, new Identifier(ModConstants.MOD_ID, ModConstants.VOID_TOTEM_STRING),
				VOID_TOTEM_ITEM);
		CONFIG = VoidTotemConfig.setup();

		if (FabricLoader.getInstance().isModLoaded(ModConstants.CURIOS_MOD_ID)) {
			CuriosApi.enqueueSlotType(BuildScheme.REGISTER, SlotTypePreset.CHARM.getInfoBuilder().build());
			LOGGER.debug("Enqueued IMC to {}", ModConstants.CURIOS_MOD_ID);
		}
	}
}
