package com.affehund.voidtotem.core;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.VoidTotem;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.*;

public class ModUtils {
    public static boolean isModLoaded(String modID) {
        return ModList.get() != null && ModList.get().getModContainerById(modID).isPresent();
    }

    public static boolean tryUseVoidTotem(LivingEntity livingEntity, DamageSource source) {
        if (!isDimensionBlacklisted(livingEntity) && isOutOfWorld(source, livingEntity)) {
            if (livingEntity instanceof ServerPlayer player && player.connection.awaitingPositionFromClient == null) {
                player.connection.aboveGroundTickCount = 0;

                ItemStack stack = getTotemItemStack(player);

                if (stack != null) {
                    giveUseStatAndCriterion(stack, player);
                    stack = damageOrShrinkItemStack(stack, player);

                    if (player.isVehicle()) player.ejectPassengers();

                    player.stopRiding();
                    player.getPersistentData().putBoolean(ModConstants.NBT_TAG, true);
                    player.setHealth(1.0f);

                    teleportToSavePosition(player);
                    sendTotemEffectPacket(stack, player);
                    return true;
                }
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

    public static ItemStack getTotemItemStack(ServerPlayer player) {
        if (VoidTotemConfig.COMMON_CONFIG.NEEDS_TOTEM.get()) {
            var possibleTotemStacks = filterPossibleTotemStacks(getTotemFromTrinkets(player), getTotemFromInventory(player), getTotemFromHands(player));
            return possibleTotemStacks.stream().findFirst().orElse(null);
        }
        return ItemStack.EMPTY;
    }

    public static List<ItemStack> filterPossibleTotemStacks(ItemStack... stacks) {
        return Arrays.stream(stacks).filter(Objects::nonNull).toList();
    }

    public static ItemStack findCuriosItem(Item item, LivingEntity livingEntity) {
        return CuriosApi.getCuriosHelper().findFirstCurio(livingEntity, item).map(SlotResult::stack).orElse(null);
    }

    public static ItemStack getTotemFromTrinkets(ServerPlayer player) {
        if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) {
            var additionalTotems = new HashSet<>(Collections.singleton(VoidTotem.VOID_TOTEM_ITEM.get()));

            if (!VoidTotemTags.ADDITIONAL_TOTEMS.getValues().isEmpty())
                additionalTotems.addAll(VoidTotemTags.ADDITIONAL_TOTEMS.getValues());

            for (var additionalTotem : additionalTotems) {
                var curiosTotemStack = ModUtils.findCuriosItem(additionalTotem, player);
                if (curiosTotemStack != null) return curiosTotemStack;
            }
        }
        return null;
    }

    public static ItemStack getTotemFromInventory(ServerPlayer player) {
        if (VoidTotemConfig.COMMON_CONFIG.USE_TOTEM_FROM_INVENTORY.get()) {
            for (ItemStack stack : player.getInventory().items) {
                if (ModUtils.isVoidTotemOrAdditionalTotem(stack)) return stack;
            }
        }
        return null;
    }

    public static ItemStack getTotemFromHands(ServerPlayer player) {
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

    public static ItemStack damageOrShrinkItemStack(ItemStack stack, ServerPlayer player) {
        var copiedStack = stack.copy();
        if (stack.isDamageableItem()) {
            stack.hurt(1, player.getRandom(), player);
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

    public static void teleportToSavePosition(ServerPlayer player) {
        long lastBlockPos = player.getPersistentData().getLong(ModConstants.LAST_BLOCK_POS);
        var teleportPos = BlockPos.of(lastBlockPos);

        var positionInRadius = positionInRadius(player, teleportPos);
        if (positionInRadius == null) {
            player.teleportTo(teleportPos.getX(), player.level.getMaxBuildHeight() + VoidTotemConfig.COMMON_CONFIG.TELEPORT_HEIGHT_OFFSET.get(), teleportPos.getZ());
            player.connection.aboveGroundTickCount = 0;
        }
    }

    public static BlockPos positionInRadius(ServerPlayer player, BlockPos teleportPos) {
        for (int i = 0; i < 16; i++) {

            var maxBuildHeight = player.level.getMaxBuildHeight();

            var x = teleportPos.getX() + (player.getRandom().nextDouble() - 0.5D) * 4.0D;
            var y = Mth.clamp(player.getRandom().nextInt() * maxBuildHeight, 0.0D, maxBuildHeight - 1);
            var z = teleportPos.getZ() + (player.getRandom().nextDouble() - 0.5D) * 4.0;

            var pos = new BlockPos(x, y, z);
            if (player.randomTeleport(x, y, z, true)) {
                return pos;
            }
        }
        return null;
    }

    public static void sendTotemEffectPacket(ItemStack stack, ServerPlayer player) {
        PacketHandler.sendToPlayer(new TotemEffectPacket(stack, player), player);
        PacketHandler.sendToAllTracking(new TotemEffectPacket(stack, player), player);
    }
}