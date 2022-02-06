package com.affehund.voidtotem.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.affehund.voidtotem.core.ModConstants;
import com.affehund.voidtotem.core.ModUtils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Affehund
 *
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
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
		if (livingEntity instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = ((ServerPlayerEntity) ((Object) this));
			if (!player.world.isClient) {
				if (player.getScoreboardTags().contains(ModConstants.NBT_TAG)) { // has tag
					player.networkHandler.floatingTicks = 0;
					player.removeScoreboardTag(ModConstants.NBT_TAG); // remove tag
					info.cancel();
				}
			}
		}
	}
}
