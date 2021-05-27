package com.affehund.voidtotem;

import com.affehund.voidtotem.core.ClientPacketHandler;
import com.affehund.voidtotem.core.ModConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

/**
 * @author Affehund
 *
 */
public class VoidTotemFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPacketHandler.register();

		ItemTooltipCallback.EVENT.register((stack, context, tooltip) -> {
			if (VoidTotemFabric.CONFIG.SHOW_TOTEM_TOOLTIP) {
				Item item = stack.getItem();
				if (item.equals(VoidTotemFabric.VOID_TOTEM_ITEM)
						|| (item.equals(Items.TOTEM_OF_UNDYING) && VoidTotemFabric.CONFIG.ALLOW_TOTEM_OF_UNDYING)) {
					tooltip.add(new TranslatableText(ModConstants.TOOLTIP_VOID_TOTEM).formatted(Formatting.GREEN));
				}
			}
		});
	}
}
