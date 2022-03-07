package com.affehund.voidtotem.core;

import com.affehund.voidtotem.ModConstants;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;

public class VoidTotemTags {
    public static final Tag.Named<Item> CURIOS_CHARM = modTag(ModConstants.CURIOS_MOD_ID, ModConstants.CURIOS_CHARM_SLOT);
    public static final Tag.Named<Item> ADDITIONAL_TOTEMS = modTag(ModConstants.MOD_ID, ModConstants.ADDITIONAL_TOTEMS_TAG);

    private static Tag.Named<Item> modTag(String modId, String name) {
        return ItemTags.bind(modId + ":" + name);
    }
}
