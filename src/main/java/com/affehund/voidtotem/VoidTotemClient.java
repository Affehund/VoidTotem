package com.affehund.voidtotem;

import com.affehund.voidtotem.core.ClientPacketHandler;
import com.affehund.voidtotem.core.ModConstants;
import com.affehund.voidtotem.core.ModUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.List;

@Environment(EnvType.CLIENT)
public class VoidTotemClient implements ClientModInitializer {
    private static void getTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip) {
        if (VoidTotem.CONFIG.SHOW_TOTEM_TOOLTIP) {
            Item item = stack.getItem();
            if (item.equals(VoidTotem.VOID_TOTEM_ITEM) || ModUtils.isAdditionalTotem(stack)) {
                tooltip.add(new TranslatableText(ModConstants.TOOLTIP_VOID_TOTEM).formatted(Formatting.GREEN));
            }
        }
    }

    @Override
    public void onInitializeClient() {
        ClientPacketHandler.register();
        ItemTooltipCallback.EVENT.register(VoidTotemClient::getTooltip);
    }
}
