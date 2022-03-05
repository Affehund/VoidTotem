package com.affehund.voidtotem.mixin;

import com.affehund.voidtotem.core.ILivingEntityMixinAccessor;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.affehund.voidtotem.core.ModConstants;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Affehund
 *
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ILivingEntityMixinAccessor {

	@Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
	private void handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> info) {
		LivingEntity livingEntity = (LivingEntity) (Object) this;
			if (!livingEntity.world.isClient) {
				if (livingEntity.getScoreboardTags().contains(ModConstants.NBT_TAG)) { // has tag
					if (livingEntity instanceof ServerPlayerEntity player)
					player.networkHandler.floatingTicks = 0;
					livingEntity.removeScoreboardTag(ModConstants.NBT_TAG); // remove tag
					info.cancel();
				}
			}
	}

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
				if (entity.isSubmergedInWater() || (entity instanceof ServerPlayerEntity player && player.getAbilities().flying) || (entity instanceof ServerPlayerEntity player2 && player2.getAbilities().allowFlying)
						|| entity.world.getBlockState(pos).getBlock() == Blocks.COBWEB)
					entity.removeScoreboardTag(ModConstants.NBT_TAG);
			}
		}
	}
}
