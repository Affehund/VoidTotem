package com.affehund.voidtotem;

import net.minecraft.resources.ResourceLocation;

public class ModConstants {
    public static final String MOD_ID = "voidtotem";
    public static final String MOD_NAME = "Void Totem";
    public static final String CHANNEL_NAME = "main_channel";
    public static final String IS_FALL_DAMAGE_IMMUNE = MOD_NAME + "IsFallDamageImmune";
    public static final String LAST_SAVE_BLOCK_POS = MOD_NAME + "LastSaveBlockPos";

    public static final String COMMON_CONFIG_NAME = MOD_ID + "-common.toml";

    public static final String ITEM_VOID_TOTEM = "totem_of_void_undying";
    public static final String TOOLTIP_VOID_TOTEM = "tooltip." + MOD_ID + "." + ITEM_VOID_TOTEM;

    public static final String ADVANCEMENT_ADVENTURE_TOTEM_PATH = "adventure/totem_of_undying";
    public static final String ADVANCEMENT_ADVENTURE_VOID_TOTEM_PATH = "adventure/" + ITEM_VOID_TOTEM;
    public static final String ADVANCEMENT_VOID_TOTEM_TITLE = "advancements." + MOD_ID + ".adventure." + ITEM_VOID_TOTEM + ".title";
    public static final String ADVANCEMENT_VOID_TOTEM_DESC = "advancements." + MOD_ID + ".adventure." + ITEM_VOID_TOTEM + ".description";

    public static final ResourceLocation LOCATION_END_CITY_TREASURE = new ResourceLocation("chests/pillager_outpost");
    public static final ResourceLocation LOCATION_END_CITY_TREASURE_INJECTION = new ResourceLocation(MOD_ID, "inject/chests/pillager_outpost");

    public static final String ADDITIONAL_TOTEMS_TAG = "additional_totems";

    public static final String CURIOS_CHARM_SLOT = "charm";
    public static final String CURIOS_MOD_ID = "curios";

}
