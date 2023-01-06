package com.affehund.voidtotem;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModConstants {
    public static final String MOD_ID = "voidtotem";
    public static final String MOD_NAME = "Void Totem";
    public static final String IS_FALL_DAMAGE_IMMUNE = MOD_NAME + "IsFallDamageImmune";
    public static final String LAST_SAVE_BLOCK_POS = MOD_NAME + "LastSaveBlockPos";

    public static final String ITEM_VOID_TOTEM = "totem_of_void_undying";
    public static final String TOOLTIP_VOID_TOTEM = "tooltip." + MOD_ID + "." + ITEM_VOID_TOTEM;

    public static final String ADVANCEMENT_ADVENTURE_TOTEM_PATH = "adventure/totem_of_undying";
    public static final String ADVANCEMENT_ADVENTURE_VOID_TOTEM_PATH = "adventure/" + ITEM_VOID_TOTEM;
    public static final String ADVANCEMENT_VOID_TOTEM_TITLE = "advancements." + MOD_ID + ".adventure." + ITEM_VOID_TOTEM + ".title";
    public static final String ADVANCEMENT_VOID_TOTEM_DESC = "advancements." + MOD_ID + ".adventure." + ITEM_VOID_TOTEM + ".description";

    public static final ResourceLocation TOTEM_EFFECT_PACKET_LOCATION = new ResourceLocation(MOD_ID, "totem_effect_packet");
    public static final ResourceLocation END_CITY_TREASURE_INJECTION_LOCATION = new ResourceLocation(MOD_ID, "inject/chests/end_city_treasure");

    public static final String CURIOS_MOD_ID = "curios";
    public static final String TRINKETS_MOD_ID = "trinkets";

    public static final TagKey<Item> ADDITIONAL_TOTEMS_TAG = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "additional_totems"));
    public static final TagKey<Item> CURIOS_CHARM_TAG = TagKey.create(Registries.ITEM, new ResourceLocation(ModConstants.CURIOS_MOD_ID, "charm"));
    public static final TagKey<Item> TRINKETS_CHARM_TAG = TagKey.create(Registries.ITEM, new ResourceLocation(ModConstants.TRINKETS_MOD_ID, "charm/charm"));
}