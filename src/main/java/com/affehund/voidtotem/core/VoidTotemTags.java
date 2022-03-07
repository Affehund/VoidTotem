package com.affehund.voidtotem.core;

import com.affehund.voidtotem.ModConstants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class VoidTotemTags {
    public static final TagKey<Item> CURIOS_CHARM = modTag(ModConstants.CURIOS_MOD_ID, ModConstants.CURIOS_CHARM_SLOT);
    public static final TagKey<Item> ADDITIONAL_TOTEMS = modTag(ModConstants.MOD_ID, ModConstants.ADDITIONAL_TOTEMS_TAG);

    private static TagKey<Item> modTag(String modId, String name) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(modId, name));
    }
}
