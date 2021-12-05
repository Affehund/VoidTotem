package com.affehund.voidtotem.core.network;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.core.ModUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    /**
     * This creates the simple channel to handle the packets of this mod.
     */
    public static final SimpleChannel MOD_CHANNEL = NetworkRegistry.newSimpleChannel(
            ModUtils.getModResourceLocation(ModConstants.CHANNEL_NAME), () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    /**
     * This registers the packets of the mod.
     */
    public static void registerMessages() {
        int index = 0;
        MOD_CHANNEL.registerMessage(index++, TotemEffectPacket.class, TotemEffectPacket::encode, TotemEffectPacket::new,
                TotemEffectPacket::handle);
    }

    /**
     * Used to send a message to the given player.
     *
     * @param <MSG>   the message
     * @param message MSG
     * @param player  ServerPlayerEntity
     */
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        MOD_CHANNEL.sendTo(message, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    /**
     * Used to send a message to all players around the given player.
     *
     * @param <MSG>   the message
     * @param message MSG
     * @param entity  ServerPlayerEntity
     */
    public static <MSG> void sendToAllTracking(MSG message, ServerPlayer entity) {
        MOD_CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }
}
