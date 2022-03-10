package com.affehund.voidtotem.mixin;

import com.affehund.voidtotem.core.ILivingEntityMixinAccessor;
import com.affehund.voidtotem.core.ModConstants;
import com.affehund.voidtotem.core.ModUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LivingEntity.class)
public class LivingEntityMixin implements ILivingEntityMixinAccessor {
    private long blockPos;

    @Override
    public long setBlockPosAsLong(long pos) {
        return blockPos = pos;
    }

    @Override
    public long getBlockPosAsLong() {
        return blockPos;
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound tag, CallbackInfo ci) {
        blockPos = tag.getLong(ModConstants.LAST_BLOCK_POS);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound tag, CallbackInfo ci) {
        tag.putLong(ModConstants.LAST_BLOCK_POS, blockPos);
    }

    @Inject(method = "tryUseTotem(Lnet/minecraft/entity/damage/DamageSource;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void tryUseTotem(DamageSource source, CallbackInfoReturnable<Boolean> ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (ModUtils.tryUseVoidTotem(livingEntity, source)) {
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> info) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (livingEntity instanceof ServerPlayerEntity player) {
            if (!player.world.isClient) {
                if (player.getScoreboardTags().contains(ModConstants.NBT_TAG)) {
                    player.networkHandler.floatingTicks = 0;
                    player.removeScoreboardTag(ModConstants.NBT_TAG);
                    info.cancel();
                }
            }
        }
    }

    @Inject(method = "tick()V", at = @At(value = "TAIL"))
    private void tick(CallbackInfo callbackInfo) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!entity.world.isClient()) {
            BlockPos pos = new BlockPos(entity.getPos());

            long lastPosLong = ((ILivingEntityMixinAccessor) entity).getBlockPosAsLong();
            BlockPos lastPos = BlockPos.fromLong(lastPosLong);
            if (entity.world.getBlockState(pos.down()).isSolidBlock(entity.world, pos.down())) {
                if (!lastPos.equals(pos)) {
                    ((ILivingEntityMixinAccessor) entity).setBlockPosAsLong(pos.asLong());
                }
            }

            if (entity.getScoreboardTags().contains(ModConstants.NBT_TAG)) {
                if (entity instanceof ServerPlayerEntity player)
                player.networkHandler.floatingTicks = 0;
                if (entity.isSubmergedInWater() || entity instanceof ServerPlayerEntity player && (player.getAbilities().flying || player.getAbilities().allowFlying)
                        || entity.world.getBlockState(pos).getBlock() == Blocks.COBWEB)
                    entity.removeScoreboardTag(ModConstants.NBT_TAG);
            }
        }
    }
}
