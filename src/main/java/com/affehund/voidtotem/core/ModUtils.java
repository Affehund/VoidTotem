package com.affehund.voidtotem.core;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import com.affehund.voidtotem.ModConstants;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * A class with some utilities methods for the mod.
 * 
 * @author Affehund
 *
 */
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
	 * @apiNote only use if the curios mod is loaded @see ModUtils#isModLoaded
	 * 
	 * @param item   Item
	 * @param living LivingEntity
	 * @return an ItemStack with a given item.
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
