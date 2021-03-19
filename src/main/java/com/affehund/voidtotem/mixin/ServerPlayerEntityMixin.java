package com.affehund.voidtotem.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.affehund.voidtotem.ModConstants;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Affehund
 *
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
		throw new UnsupportedOperationException("You can't make a instance of the mixin.");
	}

	@Inject(method = "playerTick()V", at = @At(value = "TAIL"))
	private void tick(CallbackInfo callbackInfo) {
		ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
		if (!player.world.isClient()) {
			if (player.getScoreboardTags().contains(ModConstants.NBT_TAG)) {
				player.networkHandler.floatingTicks = 0;
				BlockPos pos = this.getBlockPos();
				if (player.isSubmergedInWater() || player.abilities.flying || player.abilities.allowFlying
						|| player.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
					player.removeScoreboardTag(ModConstants.NBT_TAG);
					return;
				}
			}
		}
	}
}
