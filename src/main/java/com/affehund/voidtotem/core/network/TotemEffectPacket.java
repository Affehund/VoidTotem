package com.affehund.voidtotem.core.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TotemEffectPacket {
    private final ItemStack stack;
    private final Entity entity;

    public TotemEffectPacket(FriendlyByteBuf buf) {
        Minecraft mc = Minecraft.getInstance();
        this.stack = buf.readItem();
        assert mc.level != null;
        this.entity = mc.level.getEntity(buf.readInt());
    }

    public TotemEffectPacket(ItemStack stack, Entity entity) {
        this.stack = stack;
        this.entity = entity;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeItem(stack);
        buf.writeInt(entity.getId());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handlePlayActivateAnimation));
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handlePlayActivateAnimation() {
        Minecraft mc = Minecraft.getInstance();
        mc.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
        var level = mc.level;
        if (level != null) {
            level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
        }

        if (entity == mc.player) {
            mc.gameRenderer.displayItemActivation(stack);
        }
    }
}
