package com.affehund.voidtotem.network;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class VoidTotemPayloadHandler {
    private static final VoidTotemPayloadHandler INSTANCE =
            new VoidTotemPayloadHandler();

    public static VoidTotemPayloadHandler getInstance() {
        return INSTANCE;
    }

    public void handleUseTotem(TotemEffectPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> TotemEffectPacket.handle(msg))
                .exceptionally(e -> {
                    ctx.disconnect(Component.translatable("voidtotem.networking.failed", e.getMessage()));
                    return null;
                });
    }
}
