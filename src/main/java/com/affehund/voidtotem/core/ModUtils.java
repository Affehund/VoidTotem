package com.affehund.voidtotem.core;

import com.affehund.voidtotem.ModConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;

public class ModUtils {
    /**
     * Used to check whether a given mod is loaded.
     *
     * @param modID String
     * @return whether the mod is loaded.
     */
    public static boolean isModLoaded(String modID) {
        return ModList.get() != null && ModList.get().getModContainerById(modID).isPresent();
    }

    /**
     * Used to find an item in a curios inventory.
     *
     * @param item       Item
     * @param livingItem LivingEntity
     * @return an ItemStack with a given item.
     * @apiNote only use if the curios mod is loaded @see ModUtils#isModLoaded
     */
    public static ItemStack findCuriosItem(Item item, LivingEntity livingItem) {
        return CuriosApi.getCuriosHelper().findEquippedCurio(item, livingItem).map(ImmutableTriple::getRight)
                .orElse(ItemStack.EMPTY);
    }

    /**
     * Used to return the mod resource location for a given string (f.ex.:
     * voidtotem:textures/item/totem_of_void_undying).
     *
     * @param path String
     * @return ResourceLocation
     */
    public static ResourceLocation getModResourceLocation(String path) {
        return new ResourceLocation(ModConstants.MOD_ID, path);
    }
}