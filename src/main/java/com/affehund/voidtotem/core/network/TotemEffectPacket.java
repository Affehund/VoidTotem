package com.affehund.voidtotem.core.network;

import java.util.function.Supplier;

import com.affehund.voidtotem.VoidTotem;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * A class to send a packet from the server to the given entity to play the
 * totem animation.
 * 
 * @see VoidTotem#playActivateAnimation
 * 
 * @author Affehund
 *
 */
public class TotemEffectPacket {
	private ItemStack itemStack;
	private Entity entity;

	public TotemEffectPacket(PacketBuffer buf) {
		Minecraft mc = Minecraft.getInstance();
		this.itemStack = buf.readItem();
		this.entity = mc.level.getEntity(buf.readInt());
	}

	public TotemEffectPacket(ItemStack itemStack, Entity entity) {
		this.itemStack = itemStack;
		this.entity = entity;
	}

	/**
	 * Used to encode the packet.
	 * 
	 * @param buf PacketBuffer
	 */
	public void encode(PacketBuffer buf) {
		buf.writeItem(itemStack);
		buf.writeInt(entity.getId());
	}

	/**
	 * Used to handle the packet.
	 * 
	 * @param context Supplier<NetworkEvent.Context
	 */
	@SuppressWarnings("deprecation")
	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
				VoidTotem.INSTANCE.playActivateAnimation(this.itemStack, this.entity);
			});
		});
		context.get().setPacketHandled(true);
	}
}
