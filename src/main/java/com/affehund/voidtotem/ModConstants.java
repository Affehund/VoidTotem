package com.affehund.voidtotem;

import net.minecraft.resources.ResourceLocation;

public class ModConstants {
    public static final String MOD_ID = "voidtotem";
    public static final String MOD_NAME = "Void Totem";
    public static final String CHANNEL_NAME = "main_channel";
    public static final String NBT_TAG = MOD_ID + "_living_falling";
    public static final String LAST_BLOCK_POS = MOD_ID + "_last_block_pos";

    public static final String COMMON_CONFIG_NAME = MOD_ID + "-common.toml";

    public static final String ITEM_VOID_TOTEM = "totem_of_void_undying";
    public static final String TOOLTIP_VOID_TOTEM = "tooltip." + MOD_ID + "." + ITEM_VOID_TOTEM;

    public static final String CRITERIA_USED_VOID_TOTEM = "criteria_used_" + ITEM_VOID_TOTEM;
    public static final String ADVANCEMENT_ADVENTURE_TOTEM_PATH = "adventure/totem_of_undying";
    public static final String ADVANCEMENT_ADVENTURE_VOID_TOTEM_PATH = "adventure/" + ITEM_VOID_TOTEM;
    public static final String ADVANCEMENT_VOID_TOTEM_TITLE = "advancements." + MOD_ID + ".adventure." + ITEM_VOID_TOTEM
            + ".title";
    public static final String ADVANCEMENT_VOID_TOTEM_DESC = "advancements." + MOD_ID + ".adventure." + ITEM_VOID_TOTEM
            + ".description";

    public static final ResourceLocation LOCATION_END_CITY_TREASURE = new ResourceLocation("chests/end_city_treasure");
    public static final ResourceLocation LOCATION_END_CITY_TREASURE_INJECTION = new ResourceLocation(MOD_ID,
            "inject/end_city_treasure");

    public static final String CURIOS_CHARM_SLOT = "charm";
    public static final String CURIOS_MOD_ID = "curios";
}
