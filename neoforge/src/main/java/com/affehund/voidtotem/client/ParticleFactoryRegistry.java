package com.affehund.voidtotem.client;

import com.affehund.voidtotem.VoidTotemForge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleFactoryRegistry {

    @SubscribeEvent
    public static void registerParticleProvidersEvent(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(VoidTotemForge.VOID_TOTEM_PARTICLE.get(), VoidTotemParticle.Provider::new);
    }
}
