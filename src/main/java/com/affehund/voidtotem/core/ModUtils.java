package com.affehund.voidtotem.core;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * @author Affehund
 *
 */
public class ModUtils {
	public static boolean isModLoaded(String modID) {
		return ModList.get() != null && ModList.get().getModContainerById(modID).isPresent();
	}

	public static ItemStack findCuriosItem(Item item, LivingEntity living) {
		return CuriosApi.getCuriosHelper().findEquippedCurio(item, living).map(ImmutableTriple::getRight)
				.orElse(ItemStack.EMPTY);
	}
}
