package com.affehund.voidtotem.core;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.VoidTotem;
import com.affehund.voidtotem.api.VoidTotemEvent;
import com.affehund.voidtotem.core.network.PacketHandler;
import com.affehund.voidtotem.core.network.TotemEffectPacket;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ModUtils {
    public static boolean isModLoaded(String modID) {
        return ModList.get() != null && ModList.get().getModContainerById(modID).isPresent();
    }

    public static boolean tryUseVoidTotem(LivingEntity livingEntity, DamageSource source) {
        if (!isDimensionBlacklisted(livingEntity) && isOutOfWorld(source, livingEntity)) {
            if (isAwaitingPositionFromClient(livingEntity)) return false;

            var stack = getTotemItemStack(livingEntity);

            if (stack != null) {

                var event = new VoidTotemEvent(stack, livingEntity, source);
                MinecraftForge.EVENT_BUS.post(event);
                if (event.getResult().equals(Event.Result.ALLOW)) return true;
                if (event.getResult().equals(Event.Result.DENY)) return false;

                if (livingEntity instanceof ServerPlayer player) {
                    player.connection.aboveGroundTickCount = 0;
                    giveUseStatAndCriterion(stack, player);
                }

                stack = damageOrShrinkItemStack(stack, livingEntity);

                if (livingEntity.isVehicle()) livingEntity.ejectPassengers();

                livingEntity.stopRiding();
                livingEntity.setHealth(1.0f);
                livingEntity.getPersistentData().putBoolean(ModConstants.IS_FALL_DAMAGE_IMMUNE, true);

                teleportToSavePosition(livingEntity);
                sendTotemEffectPacket(stack, livingEntity);
                return true;
            }
        }
        return false;
    }

    public static boolean isDimensionBlacklisted(LivingEntity livingEntity) {
        return VoidTotemConfig.COMMON_CONFIG.BLACKLISTED_DIMENSIONS.get().contains(livingEntity.level.dimension().location().toString());
    }

    public static boolean isOutOfWorld(DamageSource source, LivingEntity livingEntity) {
        return source.equals(DamageSource.OUT_OF_WORLD) && livingEntity.getY() < livingEntity.level.getMinBuildHeight();
    }

    public static boolean isAwaitingPositionFromClient(LivingEntity livingEntity) {
        return livingEntity instanceof ServerPlayer player && player.connection.awaitingPositionFromClient != null;
    }

    public static ItemStack getTotemItemStack(LivingEntity player) {
        if (VoidTotemConfig.COMMON_CONFIG.NEEDS_TOTEM.get()) {
            var possibleTotemStacks = filterPossibleTotemStacks(getTotemFromCurios(player), getTotemFromInventory(player), getTotemFromHands(player));
            return possibleTotemStacks.stream().findFirst().orElse(null);
        }
        return ItemStack.EMPTY;
    }

    public static List<ItemStack> filterPossibleTotemStacks(ItemStack... stacks) {
        return Arrays.stream(stacks).filter(Objects::nonNull).toList();
    }

    public static ItemStack getTotemFromCurios(LivingEntity livingEntity) {
        if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) {
            return ModUtils.findCuriosItem(livingEntity, ModUtils::isVoidTotemOrAdditionalTotem);
        }
        return null;
    }

    public static ItemStack findCuriosItem(LivingEntity livingEntity, Predicate<ItemStack> filter) {
        return CuriosApi.getCuriosHelper().findFirstCurio(livingEntity, filter).map(SlotResult::stack).orElse(null);
    }

    public static ItemStack getTotemFromInventory(LivingEntity livingEntity) {
        if (VoidTotemConfig.COMMON_CONFIG.USE_TOTEM_FROM_INVENTORY.get() && livingEntity instanceof ServerPlayer player) {
            for (ItemStack stack : player.getInventory().items) {
                if (ModUtils.isVoidTotemOrAdditionalTotem(stack)) return stack;
            }
        }
        return null;
    }

    public static ItemStack getTotemFromHands(LivingEntity player) {
        for (var hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (ModUtils.isVoidTotemOrAdditionalTotem(stack)) return stack;
        }
        return null;
    }

    public static boolean isVoidTotemOrAdditionalTotem(ItemStack stack) {
        return isVoidTotem(stack) || isAdditionalTotem(stack);
    }

    public static boolean isVoidTotem(ItemStack stack) {
        return stack.getItem().equals(VoidTotem.VOID_TOTEM_ITEM.get());
    }

    public static boolean isAdditionalTotem(ItemStack stack) {
        return stack.is(VoidTotemTags.ADDITIONAL_TOTEMS);
    }

    public static ItemStack damageOrShrinkItemStack(ItemStack stack, LivingEntity livingEntity) {
        var copiedStack = stack.copy();
        if (stack.isDamageableItem()) {
            stack.hurtAndBreak(1, livingEntity, e -> e.broadcastBreakEvent(livingEntity.getUsedItemHand()));
        } else {
            stack.shrink(1);
        }
        return copiedStack;
    }

    public static void giveUseStatAndCriterion(ItemStack stack, ServerPlayer player) {
        if (!stack.isEmpty()) {
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            CriteriaTriggers.USED_TOTEM.trigger(player, stack);
        }
    }

    public static void teleportToSavePosition(LivingEntity livingEntity) {
        long lastBlockPos = livingEntity.getPersistentData().getLong(ModConstants.LAST_SAVE_BLOCK_POS);
        var teleportPos = BlockPos.of(lastBlockPos);

        var positionInRadius = positionInRadius(livingEntity, teleportPos);
        if (positionInRadius == null) {
            livingEntity.teleportTo(teleportPos.getX(), livingEntity.level.getMaxBuildHeight() + VoidTotemConfig.COMMON_CONFIG.TELEPORT_HEIGHT_OFFSET.get(), teleportPos.getZ());
            if (livingEntity instanceof ServerPlayer player) player.connection.aboveGroundTickCount = 0;
        }
    }

    public static BlockPos positionInRadius(LivingEntity livingEntity, BlockPos lastPos) {
        BlockPos teleportPos = null;
        for (int i = 0; i < 16; i++) {

            var level = livingEntity.level;
            var x = lastPos.getX() + (livingEntity.getRandom().nextDouble() - 0.5D) * 16.0D;
            var y = Mth.clamp(lastPos.getY() + (double) (livingEntity.getRandom().nextInt(16) - 8), level.getMinBuildHeight(), level.getMaxBuildHeight() - 1);
            var z = lastPos.getZ() + (livingEntity.getRandom().nextDouble() - 0.5D) * 16.0D;

            var pos = new BlockPos(x, y, z);
            if (livingEntity.randomTeleport(x, y, z, true)) {
                teleportPos = pos;
                break;
            }
        }
        return teleportPos;
    }

    public static void sendTotemEffectPacket(ItemStack stack, LivingEntity livingEntity) {
        if (livingEntity instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(new TotemEffectPacket(stack, player), player);
        }
        PacketHandler.sendToAllTracking(new TotemEffectPacket(stack, livingEntity), livingEntity);
    }

    public static void setLastSaveBlockPos(LivingEntity livingEntity) {
        var level = livingEntity.level;
        var currentPos = livingEntity.blockPosition();
        var lastPos = BlockPos.of(livingEntity.getPersistentData().getLong(ModConstants.LAST_SAVE_BLOCK_POS));
        if (isSaveBlockPos(level, currentPos.below())) {
            if (!lastPos.equals(currentPos) || !isSaveBlockPos(level, lastPos.below())) {
                livingEntity.getPersistentData().putLong(ModConstants.LAST_SAVE_BLOCK_POS, currentPos.asLong());
            }
        }
    }

    public static boolean isSaveBlockPos(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.isRedstoneConductor(level, pos);
    }

    public static void resetFallDamageImmunity(LivingEntity livingEntity) {
        if (livingEntity.getPersistentData().getBoolean(ModConstants.IS_FALL_DAMAGE_IMMUNE)) {
            var canPlayerFly = false;
            if (livingEntity instanceof ServerPlayer player) {
                player.connection.aboveGroundTickCount = 0;
                var abilities = player.getAbilities();
                if (abilities.flying || abilities.mayfly)
                    canPlayerFly = true;
            }

            if (livingEntity.isInWater() || livingEntity.level.getBlockState(livingEntity.blockPosition()).getBlock() == Blocks.COBWEB || canPlayerFly) {
                livingEntity.getPersistentData().putBoolean(ModConstants.IS_FALL_DAMAGE_IMMUNE, false);
            }
        }
    }
}