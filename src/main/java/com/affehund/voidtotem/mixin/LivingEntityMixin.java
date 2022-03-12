package com.affehund.voidtotem.mixin;

import com.affehund.voidtotem.core.LivingEntityAccessor;
import com.affehund.voidtotem.core.ModConstants;
import com.affehund.voidtotem.core.ModUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements LivingEntityAccessor {
    private boolean isFallDamageImmune;
    private long lastSaveBlockPos;

    @Inject(method = "tryUseTotem(Lnet/minecraft/entity/damage/DamageSource;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void tryUseTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (ModUtils.tryUseVoidTotem(livingEntity, source)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        var livingEntity = (LivingEntity) (Object) this;

        if (((LivingEntityAccessor) livingEntity).isFallDamageImmune()) {
            if (livingEntity instanceof ServerPlayerEntity player) player.networkHandler.floatingTicks = 0;
            ((LivingEntityAccessor) livingEntity).setFallDamageImmune(false);
            cir.cancel();
        }
    }

    @Inject(method = "tick()V", at = @At(value = "TAIL"))
    private void tick(CallbackInfo ci) {
        var livingEntity = (LivingEntity) (Object) this;
        ModUtils.setLastSaveBlockPos(livingEntity);
        ModUtils.resetFallDamageImmunity(livingEntity);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.isFallDamageImmune = nbt.getBoolean(ModConstants.IS_FALL_DAMAGE_IMMUNE);
        this.lastSaveBlockPos = nbt.getLong(ModConstants.LAST_SAVE_BLOCK_POS);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean(ModConstants.IS_FALL_DAMAGE_IMMUNE, this.isFallDamageImmune);
        nbt.putLong(ModConstants.LAST_SAVE_BLOCK_POS, this.lastSaveBlockPos);
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
