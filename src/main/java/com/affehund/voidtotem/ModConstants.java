package com.affehund.voidtotem;

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
	public static final String CHANNEL_NAME = "main_channel";
	public static final String VOID_TOTEM_STRING = "totem_of_void_undying";
	public static final String COMMON_CONFIG_NAME = "/" + MOD_ID + ".json";
	public static final String VOID_TOTEM_TOOLTIP = MOD_ID + ".tooltip.totem_item";
	public static final String NBT_TAG = MOD_ID + "_living_falling";
	public static final String CURIOS_MOD_ID = "curios";

	public static final Identifier TOTEM_EFFECT_PACKET = new Identifier(MOD_ID, "totem_effect_packet");
}
