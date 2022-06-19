package com.affehund.voidtotem.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {

    @Accessor
    Vec3 getAwaitingPositionFromClient();

    @Accessor
    void setAboveGroundTickCount(int ticks);

    @Accessor
    int getAboveGroundTickCount();
}
