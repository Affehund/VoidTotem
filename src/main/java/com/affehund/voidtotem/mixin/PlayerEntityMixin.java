package com.affehund.voidtotem.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.affehund.voidtotem.core.IPlayerEntityMixinAccessor;
import com.affehund.voidtotem.core.ModConstants;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;

/**
 * @author Affehund
 *
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements IPlayerEntityMixinAccessor {
	private long blockPos;

	@Override
	public long setBlockPosAsLong(long pos) {
		return blockPos = pos;
	}

	@Override
	public long getBlockPosAsLong() {
		return blockPos;
	}

	@Inject(method = "readCustomDataFromTag", at = @At("TAIL"))
	public void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
		blockPos = tag.getLong(ModConstants.LAST_BLOCK_POS);
	}

	@Inject(method = "writeCustomDataToTag", at = @At("TAIL"))
	public void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
		tag.putLong(ModConstants.LAST_BLOCK_POS, blockPos);
	}
}
