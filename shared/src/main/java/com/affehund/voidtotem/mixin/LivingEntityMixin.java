package com.affehund.voidtotem.mixin;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.core.ILivingEntityMixin;
import com.affehund.voidtotem.core.ModUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements ILivingEntityMixin {
    private boolean isFallDamageImmune;
    private long lastSaveBlockPos;

    @Inject(method = "checkTotemDeathProtection(Lnet/minecraft/world/damagesource/DamageSource;)Z", at = @At("HEAD"), cancellable = true)
    private void checkTotemDeathProtection(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        var livingEntity = (LivingEntity) (Object) this;
        if (ModUtils.canProtectFromVoid(livingEntity, source)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        var livingEntity = (LivingEntity) (Object) this;

        if (((ILivingEntityMixin) livingEntity).isFallDamageImmune()) {
            if (livingEntity instanceof ServerPlayer player) {
                ((ServerGamePacketListenerImplAccessor) player.connection).setAboveGroundTickCount(0);
            }
            ((ILivingEntityMixin) livingEntity).setFallDamageImmune(false);
            cir.cancel();
        }
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        var livingEntity = (LivingEntity) (Object) this;

        ModUtils.setLastSaveBlockPos(livingEntity);
        ModUtils.resetFallDamageImmunity(livingEntity);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readCustomDataFromNbt(@NotNull CompoundTag tag, CallbackInfo ci) {
        this.isFallDamageImmune = tag.getBoolean(ModConstants.IS_FALL_DAMAGE_IMMUNE);
        this.lastSaveBlockPos = tag.getLong(ModConstants.LAST_SAVE_BLOCK_POS);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void writeCustomDataToNbt(@NotNull CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean(ModConstants.IS_FALL_DAMAGE_IMMUNE, this.isFallDamageImmune);
        tag.putLong(ModConstants.LAST_SAVE_BLOCK_POS, this.lastSaveBlockPos);
    }

    @Override
    public boolean isFallDamageImmune() {
        return this.isFallDamageImmune;
    }

    @Override
    public void setFallDamageImmune(boolean isImmune) {
        this.isFallDamageImmune = isImmune;
    }

    @Override
    public long getLastSaveBlockPosAsLong() {
        return this.lastSaveBlockPos;
    }

    @Override
    public void setLastSaveBlockPosAsLong(long pos) {
        this.lastSaveBlockPos = pos;
    }
}
