package com.affehund.voidtotem.core.network;

import com.affehund.voidtotem.ModConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel MOD_CHANNEL = NetworkRegistry.newSimpleChannel(
            ModConstants.TOTEM_EFFECT_PACKET_LOCATION, () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void registerMessages() {
        MOD_CHANNEL.registerMessage(0, TotemEffectPacket.class, TotemEffectPacket::encode, TotemEffectPacket::new,
                TotemEffectPacket::handle);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        MOD_CHANNEL.sendTo(message, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <MSG> void sendToAllTracking(MSG message, LivingEntity entity) {
        MOD_CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }
}
