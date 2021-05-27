package com.affehund.voidtotem.core;

import net.minecraft.util.Identifier;

/**
 * A class for all the mod strings (mod id, name, item name, etc...).
 * 
 * @author Affehund
 *
 */
public final class ModConstants {
	public static final String MOD_ID = "voidtotem";
	public static final String MOD_NAME = "Void Totem";
	public static final String NBT_TAG = MOD_ID + "_living_falling";
	public static final String LAST_BLOCK_POS = MOD_ID + "_last_block_pos";
	public static final String COMMON_CONFIG_NAME = "/" + MOD_ID + ".json";

	public static final String ITEM_VOID_TOTEM = "totem_of_void_undying";
	public static final String TOOLTIP_VOID_TOTEM = "tooltip." + MOD_ID + "." + ITEM_VOID_TOTEM;

	public static final Identifier IDENTIFIER_TOTEM_EFFECT_PACKET = new Identifier(MOD_ID, "totem_effect_packet");
	public static final Identifier IDENTIFIER_END_CITY_TREASURE = new Identifier("chests/end_city_treasure");
	public static final Identifier IDENTIFIER_END_CITY_TREASURE_INJECTION = new Identifier(MOD_ID,
			"inject/end_city_treasure");

	public static final String CURIOS_MOD_ID = "curios";
}
