package com.affehund.voidtotem;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModConstants {
    public static final String MOD_ID = "voidtotem";
    public static final String MOD_NAME = "VoidTotem";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final String IS_FALL_DAMAGE_IMMUNE = MOD_NAME + "IsFallDamageImmune";
    public static final String LAST_SAVE_BLOCK_POS = MOD_NAME + "LastSaveBlockPos";

    public static final String ITEM_VOID_TOTEM = "totem_of_void_undying";
    public static final String TOOLTIP_VOID_TOTEM = "tooltip." + MOD_ID + "." + ITEM_VOID_TOTEM;

    public static final String ADVANCEMENT_ADVENTURE_TOTEM_PATH = "adventure/totem_of_undying";
    public static final String ADVANCEMENT_ADVENTURE_VOID_TOTEM_PATH = "adventure/" + ITEM_VOID_TOTEM;
    public static final String ADVANCEMENT_VOID_TOTEM_TITLE = "advancements." + MOD_ID + ".adventure." + ITEM_VOID_TOTEM + ".title";
    public static final String ADVANCEMENT_VOID_TOTEM_DESC = "advancements." + MOD_ID + ".adventure." + ITEM_VOID_TOTEM + ".description";

    public static final Identifier TOTEM_EFFECT_PACKET_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID, "totem_effect_packet");

    public static final String CURIOS_MOD_ID = "curios";
    public static final String TRINKETS_MOD_ID = "trinkets";
}
