package com.affehund.voidtotem.mixin;

import com.affehund.voidtotem.core.IPlayerEntityMixinAccessor;
import com.affehund.voidtotem.core.ModConstants;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "playerTick()V", at = @At(value = "TAIL"))
    private void tick(CallbackInfo callbackInfo) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (!player.world.isClient()) {
            BlockPos pos = new BlockPos(player.getPos());

            long lastPosLong = ((IPlayerEntityMixinAccessor) player).getBlockPosAsLong();
            BlockPos lastPos = BlockPos.fromLong(lastPosLong);
            if (player.world.getBlockState(pos.down()).isSolidBlock(world, pos.down())) {
                if (!lastPos.equals(pos)) {
                    ((IPlayerEntityMixinAccessor) player).setBlockPosAsLong(pos.asLong());
                }
            }

            if (player.getScoreboardTags().contains(ModConstants.NBT_TAG)) {
                player.networkHandler.floatingTicks = 0;
                if (player.isSubmergedInWater() || player.getAbilities().flying || player.getAbilities().allowFlying
                        || player.world.getBlockState(pos).getBlock() == Blocks.COBWEB)
                    player.removeScoreboardTag(ModConstants.NBT_TAG);
            }
        }
    }
}
