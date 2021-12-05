package com.affehund.voidtotem.core.network;

import com.affehund.voidtotem.VoidTotem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TotemEffectPacket {
    private ItemStack itemStack;
    private Entity entity;

    public TotemEffectPacket(FriendlyByteBuf buf) {
        Minecraft mc = Minecraft.getInstance();
        this.itemStack = buf.readItem();
        this.entity = mc.level.getEntity(buf.readInt());
    }

    public TotemEffectPacket(ItemStack itemStack, Entity entity) {
        this.itemStack = itemStack;
        this.entity = entity;
    }

    /**
     * Used to encode the packet.
     *
     * @param buf PacketBuffer
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeItem(itemStack);
        buf.writeInt(entity.getId());
    }

    /**
     * Used to handle the packet.
     *
     * @param context Supplier<NetworkEvent.Context
     */
    @SuppressWarnings("deprecation")
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
                VoidTotem.INSTANCE.playActivateAnimation(this.itemStack, this.entity);
            });
        });
        context.get().setPacketHandled(true);
    }
}
