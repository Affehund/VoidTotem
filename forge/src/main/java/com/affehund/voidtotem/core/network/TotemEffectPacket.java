package com.affehund.voidtotem.core.network;

import com.affehund.voidtotem.core.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TotemEffectPacket {
    private final ItemStack itemStack;
    private final Entity entity;

    public TotemEffectPacket(FriendlyByteBuf buf) {
        Minecraft mc = Minecraft.getInstance();
        this.itemStack = buf.readItem();
        assert mc.level != null;
        this.entity = mc.level.getEntity(buf.readInt());
    }

    public TotemEffectPacket(ItemStack itemStack, Entity entity) {
        this.itemStack = itemStack;
        this.entity = entity;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeItem(itemStack);
        buf.writeInt(entity.getId());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ModUtils.playActivateAnimation(itemStack, entity)));
        context.get().setPacketHandled(true);
    }
}
