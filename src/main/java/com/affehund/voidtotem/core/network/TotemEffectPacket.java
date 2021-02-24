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
 * @author Affehund
 *
 */
public class TotemEffectPacket {
	private ItemStack itemStack;
	private Entity entity;

	public TotemEffectPacket(PacketBuffer buf) {
		Minecraft mc = Minecraft.getInstance();
		this.itemStack = buf.readItemStack();
		this.entity = mc.world.getEntityByID(buf.readInt());
	}

	public TotemEffectPacket(ItemStack itemStack, Entity entity) {
		this.itemStack = itemStack;
		this.entity = entity;
	}

	public void encode(PacketBuffer buf) {
		buf.writeItemStack(itemStack);
		buf.writeInt(entity.getEntityId());
	}

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
