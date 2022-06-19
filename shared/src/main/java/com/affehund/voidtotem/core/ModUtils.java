package com.affehund.voidtotem.core;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.VoidTotem;
import com.affehund.voidtotem.mixin.ServerGamePacketListenerImplAccessor;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ModUtils {
    public static boolean isModLoaded(String modId) {
        return VoidTotem.PLATFORM.isModLoaded(modId);
    }

    public static boolean canProtectFromVoid(LivingEntity livingEntity, DamageSource source) {
        var currentDim = livingEntity.level.dimension().location().toString();
        var isBlacklistedDimension = VoidTotem.PLATFORM.isInvertedBlacklist() != VoidTotem.PLATFORM.getBlacklistedDimensions().contains(currentDim);
        var isInVoid = source.equals(DamageSource.OUT_OF_WORLD) && livingEntity.getY() < livingEntity.level.getMinBuildHeight();
        var isAwaitingPositionFromClient = livingEntity instanceof ServerPlayer player && ((ServerGamePacketListenerImplAccessor) player.connection).getAwaitingPositionFromClient() != null;

        if (!isBlacklistedDimension && isInVoid && !isAwaitingPositionFromClient) {
            var itemStack = getTotemItemStack(livingEntity);

            if (itemStack != null) {
                var result = VoidTotem.PLATFORM.getVoidTotemEventResult(itemStack, livingEntity, source);
                if (result.equals(InteractionResult.CONSUME)) return false;
                if (result.equals(InteractionResult.CONSUME_PARTIAL)) return true;

                if (livingEntity instanceof ServerPlayer player) {
                    ((ServerGamePacketListenerImplAccessor) player.connection).setAboveGroundTickCount(0);
                    giveUseStatAndCriterion(itemStack, player);
                }

                itemStack = damageOrShrinkItemStack(itemStack, livingEntity);

                if (livingEntity.isVehicle()) livingEntity.ejectPassengers();

                livingEntity.stopRiding();
                livingEntity.setHealth(1.0f);
                ((ILivingEntityMixin) livingEntity).setFallDamageImmune(true);

                if (VoidTotem.PLATFORM.giveTotemEffects()) {
                    livingEntity.removeAllEffects();
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
                }

                teleportToSavePosition(livingEntity);
                VoidTotem.PLATFORM.sendTotemEffectPacket(itemStack, livingEntity);

                return true;
            }
        }
        return false;
    }

    public static ItemStack getTotemItemStack(LivingEntity livingEntity) {
        if (VoidTotem.PLATFORM.needsTotem()) {
            var possibleTotemStacks = filterPossibleTotemStacks(getTotemFromTrinkets(livingEntity), getTotemFromInventory(livingEntity), getTotemFromHands(livingEntity));
            return possibleTotemStacks.stream().findFirst().orElse(null);
        }
        return ItemStack.EMPTY;
    }

    public static List<ItemStack> filterPossibleTotemStacks(ItemStack... stacks) {
        return Arrays.stream(stacks).filter(Objects::nonNull).toList();
    }

    public static ItemStack getTotemFromTrinkets(LivingEntity livingEntity) {
        return VoidTotem.PLATFORM.getTotemFromAdditionalSlot(livingEntity, ModUtils::isVoidTotemOrAdditionalTotem);
    }

    public static ItemStack getTotemFromInventory(LivingEntity livingEntity) {
        if (VoidTotem.PLATFORM.useTotemFromInventory() && livingEntity instanceof ServerPlayer player) {
            for (var itemStack : player.getInventory().items) {
                if (ModUtils.isVoidTotemOrAdditionalTotem(itemStack)) return itemStack;
            }
        }
        return null;
    }

    public static ItemStack getTotemFromHands(LivingEntity livingEntity) {
        for (var hand : InteractionHand.values()) {
            var itemStack = livingEntity.getItemInHand(hand);
            if (ModUtils.isVoidTotemOrAdditionalTotem(itemStack)) return itemStack;
        }
        return null;
    }

    public static boolean isVoidTotemOrAdditionalTotem(ItemStack itemStack) {
        var isVoidTotem = itemStack.getItem().equals(VoidTotem.PLATFORM.getVoidTotemItem());
        var isAdditionalTotem = itemStack.is(ModConstants.ADDITIONAL_TOTEMS_TAG);
        return isVoidTotem || isAdditionalTotem;
    }

    public static ItemStack damageOrShrinkItemStack(ItemStack itemStack, LivingEntity livingEntity) {
        var copiedStack = itemStack.copy();
        if (itemStack.isDamageableItem()) {
            itemStack.hurtAndBreak(1, livingEntity, e -> e.broadcastBreakEvent(livingEntity.getUsedItemHand()));
        } else {
            itemStack.shrink(1);
        }
        return copiedStack;
    }

    public static void giveUseStatAndCriterion(ItemStack itemStack, ServerPlayer player) {
        if (!itemStack.isEmpty()) {
            player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
            CriteriaTriggers.USED_TOTEM.trigger(player, itemStack);
        }
    }

    public static void teleportToSavePosition(LivingEntity livingEntity) {
        var lastPos = BlockPos.of(((ILivingEntityMixin) livingEntity).getLastSaveBlockPosAsLong());

        var positionInRadius = teleportToPositionNearby(livingEntity, lastPos);
        if (positionInRadius == null) {
            livingEntity.teleportTo(lastPos.getX(), livingEntity.level.getMaxBuildHeight() + VoidTotem.PLATFORM.teleportHeightOffset(), lastPos.getZ());
            if (livingEntity instanceof ServerPlayer player) {
                ((ServerGamePacketListenerImplAccessor) player.connection).setAboveGroundTickCount(0);
            }
        }
    }

    public static BlockPos teleportToPositionNearby(LivingEntity livingEntity, BlockPos lastPos) {
        BlockPos teleportPos = null;
        for (int i = 0; i < 16; i++) {

            var world = livingEntity.level;
            var x = lastPos.getX() + (livingEntity.getRandom().nextDouble() - 0.5D) * 16.0D;
            var y = Mth.clamp(lastPos.getY() + (double) (livingEntity.getRandom().nextInt(16) - 8), world.getMinBuildHeight(), world.getMaxBuildHeight() - 1);
            var z = lastPos.getZ() + (livingEntity.getRandom().nextDouble() - 0.5D) * 16.0D;

            var pos = new BlockPos(x, y, z);
            if (livingEntity.randomTeleport(x, y, z, true)) {
                teleportPos = pos;
                break;
            }
        }
        return teleportPos;
    }

    public static void playActivateAnimation(ItemStack itemStack, Entity entity) {
        var mc = Minecraft.getInstance();
        mc.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);

        var level = mc.level;
        if (level != null) {
            level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
        }

        if (entity == mc.player) {
            mc.gameRenderer.displayItemActivation(itemStack);
        }
    }

    public static void setLastSaveBlockPos(LivingEntity livingEntity) {
        var world = livingEntity.level;
        var currentPos = livingEntity.blockPosition();
        var lastPos = BlockPos.of(((ILivingEntityMixin) livingEntity).getLastSaveBlockPosAsLong());
        if (isSaveBlockPos(world, currentPos.below())) {
            if (!lastPos.equals(currentPos) || !isSaveBlockPos(world, lastPos.below())) {
                ((ILivingEntityMixin) livingEntity).setLastSaveBlockPosAsLong(currentPos.asLong());
            }
        }
    }

    public static boolean isSaveBlockPos(@NotNull Level level, BlockPos pos) {
        return level.getBlockState(pos).isRedstoneConductor(level, pos);
    }

    public static void resetFallDamageImmunity(LivingEntity livingEntity) {
        if (((ILivingEntityMixin) livingEntity).isFallDamageImmune()) {
            var canPlayerFly = false;
            if (livingEntity instanceof ServerPlayer player) {
                ((ServerGamePacketListenerImplAccessor) player.connection).setAboveGroundTickCount(0);
                var abilities = player.getAbilities();
                if (abilities.flying || abilities.mayfly) {
                    canPlayerFly = true;
                }
            }

            var isInWater = livingEntity.isInWater();
            var isInCobweb = livingEntity.level.getBlockState(livingEntity.blockPosition()).getBlock().equals(Blocks.COBWEB);

            if (canPlayerFly || isInCobweb || isInWater) {
                ((ILivingEntityMixin) livingEntity).setFallDamageImmune(false);
            }
        }
    }
}
