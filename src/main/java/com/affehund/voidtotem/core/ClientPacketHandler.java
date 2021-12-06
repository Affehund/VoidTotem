package com.affehund.voidtotem.core;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

/**
 * @author Affehund
 *
 */
public class ClientPacketHandler {
	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(ModConstants.IDENTIFIER_TOTEM_EFFECT_PACKET,
				(client, handler, buf, responseSender) -> {
					ItemStack itemStack = buf.readItemStack();
					assert client.world != null;
					Entity entity = client.world.getEntityById(buf.readInt());
					client.execute(() -> ModUtils.playActivateAnimation(itemStack, entity));
				});
	}
}
