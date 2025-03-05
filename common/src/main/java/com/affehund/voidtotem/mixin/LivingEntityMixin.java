package com.affehund.voidtotem.mixin;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.core.ILivingEntityMixin;
import com.affehund.voidtotem.core.ModUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements ILivingEntityMixin {
    
    @Unique
    private boolean voidtotem$isFallDamageImmune;
    @Unique
    private long voidtotem$lastSaveBlockPos;
    @Unique
    private DimensionType voidtotem$lastSaveBlockDim;

    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
    private void voidtotem$checkTotemDeathProtection(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        var livingEntity = (LivingEntity) (Object) this;
        if (ModUtils.canProtectFromVoid(livingEntity, source)) {
            ModUtils.handleVoidTotem(livingEntity);
            cir.setReturnValue(true);
        } else if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && ModUtils.hasVoidTotem(livingEntity)) { // check if this works (also with other mods...)
            ModUtils.handleDefaultTotemActivation(livingEntity);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void voidtotem$causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        var livingEntity = (LivingEntity) (Object) this;

        if (((ILivingEntityMixin) livingEntity).voidtotem$isFallDamageImmune()) {
            if (livingEntity instanceof ServerPlayer player) {
                ((ServerGamePacketListenerImplAccessor) player.connection).setAboveGroundTickCount(0);
            }
            ((ILivingEntityMixin) livingEntity).voidtotem$setFallDamageImmune(false);
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        var livingEntity = (LivingEntity) (Object) this;

        ModUtils.setLastSaveBlockPos(livingEntity);
        ModUtils.resetFallDamageImmunity(livingEntity);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readCustomDataFromNbt(@NotNull CompoundTag tag, CallbackInfo ci) {
        this.voidtotem$isFallDamageImmune = tag.getBoolean(ModConstants.IS_FALL_DAMAGE_IMMUNE);
        this.voidtotem$lastSaveBlockPos = tag.getLong(ModConstants.LAST_SAVE_BLOCK_POS);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void writeCustomDataToNbt(@NotNull CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean(ModConstants.IS_FALL_DAMAGE_IMMUNE, this.voidtotem$isFallDamageImmune);
        tag.putLong(ModConstants.LAST_SAVE_BLOCK_POS, this.voidtotem$lastSaveBlockPos);
    }

    @Override
    public boolean voidtotem$isFallDamageImmune() {
        return this.voidtotem$isFallDamageImmune;
    }

    @Override
    public void voidtotem$setFallDamageImmune(boolean isImmune) {
        this.voidtotem$isFallDamageImmune = isImmune;
    }

    @Override
    public long voidtotem$getLastSaveBlockPosAsLong() {
        return this.voidtotem$lastSaveBlockPos;
    }

    @Override
    public void voidtotem$setLastSaveBlockPosAsLong(long pos) {
        this.voidtotem$lastSaveBlockPos = pos;
    }

    @Override
    public DimensionType voidtotem$getLastSaveBlockDim() {
        return this.voidtotem$lastSaveBlockDim;
    }

    @Override
    public void voidtotem$setLastSaveBlockDim(DimensionType dimensionType) {
        this.voidtotem$lastSaveBlockDim = dimensionType;
    }
}
