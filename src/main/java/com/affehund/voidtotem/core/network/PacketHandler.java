package com.affehund.voidtotem.core.network;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.VoidTotem;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
	private static final String PROTOCOL_VERSION = "1";

	/**
	 * This creates the simple channel to handle the packets of this mod.
	 */
	public static final SimpleChannel MOD_CHANNEL = NetworkRegistry.newSimpleChannel(
			VoidTotem.getModResourceLocation(ModConstants.CHANNEL_NAME), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	/**
	 * This registers the packets of the mod.
	 */
	public static void registerMessages() {
		int index = 0;
		MOD_CHANNEL.registerMessage(index++, TotemEffectPacket.class, TotemEffectPacket::encode, TotemEffectPacket::new,
				TotemEffectPacket::handle);
	}

	public static <MSG> void sendToPlayer(MSG message, ServerPlayerEntity player) {
		MOD_CHANNEL.sendTo(message, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
	}

	public static <MSG> void sendToAllTracking(MSG message, ServerPlayerEntity entity) {
		MOD_CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
	}
}
