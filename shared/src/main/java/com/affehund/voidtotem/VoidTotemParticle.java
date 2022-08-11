package com.affehund.voidtotem;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class VoidTotemParticle extends SimpleAnimatedParticle {
    VoidTotemParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet spriteSet) {
        super(level, x, y, z, spriteSet, 1.25F);
        this.friction = 0.6F;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize *= 0.75F;
        this.lifetime = 60 + this.random.nextInt(12);
        this.setSpriteFromAge(spriteSet);

        if (this.random.nextInt(4) == 0) {
            // #0F4085
            this.setColor(this.random.nextFloat() * 0.1F, 0.2F + this.random.nextFloat() * 0.1F, 0.4F + this.random.nextFloat() * 0.2F);
        } else {
            // base green color
            this.setColor(0.1F + this.random.nextFloat() * 0.2F, 0.4F + this.random.nextFloat() * 0.3F, this.random.nextFloat() * 0.2F);
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(@NotNull SimpleParticleType particleType, @NotNull ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new VoidTotemParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
