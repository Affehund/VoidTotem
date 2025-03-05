package com.affehund.voidtotem.network;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.core.ModUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record TotemEffectPacket(ItemStack itemStack, int entityId) implements CustomPacketPayload {

    public static final Type<TotemEffectPacket> TYPE = new Type<>(
            ModConstants.TOTEM_EFFECT_PACKET_LOCATION);
    public static final StreamCodec<RegistryFriendlyByteBuf, TotemEffectPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC,
                    TotemEffectPacket::itemStack,
                    ByteBufCodecs.INT,
                    TotemEffectPacket::entityId,
                    TotemEffectPacket::new);

    public static void handle(TotemEffectPacket msg) {
        ModUtils.playActivateAnimation(msg.itemStack, msg.entityId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
