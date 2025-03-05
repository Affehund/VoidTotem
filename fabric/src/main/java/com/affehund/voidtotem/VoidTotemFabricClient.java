package com.affehund.voidtotem;

import com.affehund.voidtotem.client.VoidTotemParticle;
import com.affehund.voidtotem.network.TotemEffectPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class VoidTotemFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(TotemEffectPacket.TYPE,
                (payload, context) -> context.client().execute(() -> TotemEffectPacket.handle(payload)));

        ParticleFactoryRegistry.getInstance().register(VoidTotemFabric.VOID_TOTEM_PARTICLE, VoidTotemParticle.Provider::new);
    }
}
