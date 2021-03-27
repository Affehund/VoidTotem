package com.affehund.voidtotem.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.VoidTotemFabric;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

/**
 * @author Affehund
 *
 */
@Mixin(Item.class)
public class ItemMixin {
	@Inject(method = "appendTooltip(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/client/item/TooltipContext;)V", at = @At("HEAD"))
	public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext,
			CallbackInfo info) {
		if (VoidTotemFabric.CONFIG.SHOW_TOTEM_TOOLTIP) {
			Item stack = (Item) (Object) this;
			if (stack == VoidTotemFabric.VOID_TOTEM_ITEM
					|| (stack == Items.TOTEM_OF_UNDYING && VoidTotemFabric.CONFIG.ALLOW_TOTEM_OF_UNDYING)) {
				tooltip.add(new TranslatableText(ModConstants.TOOLTIP_VOID_TOTEM).formatted(Formatting.GREEN));
			}
		}
	}
}
