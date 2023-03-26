package com.affehund.voidtotem.client;

import com.affehund.voidtotem.VoidTotemForge;
import com.affehund.voidtotem.VoidTotemParticle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleFactoryRegistry {

    @SubscribeEvent
    public static void onParticleFactoryRegistration(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(VoidTotemForge.VOID_TOTEM_PARTICLE.get(),
                VoidTotemParticle.Provider::new);
    }
}
