package com.affehund.voidtotem.core;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.VoidTotem;
import com.affehund.voidtotem.api.VoidTotemEvent;
import com.affehund.voidtotem.core.network.PacketHandler;
import com.affehund.voidtotem.core.network.TotemEffectPacket;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
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
            if (livingEntity instanceof ServerPlayer player && player.connection.awaitingPositionFromClient == null) {
                player.connection.aboveGroundTickCount = 0;
            }
                ItemStack stack = getTotemItemStack(livingEntity);

                if (stack != null) {
                    var event = new VoidTotemEvent(stack, livingEntity, source);
                    MinecraftForge.EVENT_BUS.post(event);
                    if (event.getResult().equals(Event.Result.ALLOW)) return true;
                    if (event.getResult().equals(Event.Result.DENY)) return false;
                    if (livingEntity instanceof ServerPlayer player)
                        giveUseStatAndCriterion(stack, player);
                    stack = damageOrShrinkItemStack(stack, livingEntity);

                    if (livingEntity.isVehicle()) livingEntity.ejectPassengers();

                    livingEntity.stopRiding();
                    livingEntity.getPersistentData().putBoolean(ModConstants.NBT_TAG, true);
                    livingEntity.setHealth(1.0f);

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

    public static ItemStack getTotemItemStack(LivingEntity entity) {
        if (VoidTotemConfig.COMMON_CONFIG.NEEDS_TOTEM.get()) {
            List<ItemStack> possibleTotemStacks;
            if (entity instanceof ServerPlayer player) {
                 possibleTotemStacks = filterPossibleTotemStacks(getTotemFromCurios(player), getTotemFromInventory(player), getTotemFromHands(player));
            } else {
                possibleTotemStacks = filterPossibleTotemStacks(getTotemFromHands(entity));
            }
            return possibleTotemStacks.stream().findFirst().orElse(null);
        }
        return ItemStack.EMPTY;
    }

    public static List<ItemStack> filterPossibleTotemStacks(ItemStack... stacks) {
        return Arrays.stream(stacks).filter(Objects::nonNull).toList();
    }

    public static ItemStack findCuriosItem(LivingEntity livingEntity, Predicate<ItemStack> filter) {
        return CuriosApi.getCuriosHelper().findFirstCurio(livingEntity, filter).map(SlotResult::stack).orElse(null);
    }

    public static ItemStack getTotemFromCurios(ServerPlayer player) {
        if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) {
            return ModUtils.findCuriosItem(player, stack -> stack.is(VoidTotemTags.ADDITIONAL_TOTEMS) || stack.is(VoidTotem.VOID_TOTEM_ITEM.get()));
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

    public static ItemStack getTotemFromHands(LivingEntity entity) {
        for (var hand : InteractionHand.values()) {
            ItemStack stack = entity.getItemInHand(hand);
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
            if (livingEntity instanceof ServerPlayer player)
                stack.hurt(1, livingEntity.getRandom(), player);
            else {
                stack.setDamageValue(stack.getDamageValue()+1);
                if (stack.getDamageValue() > stack.getMaxDamage())
                    stack.shrink(1);
            }
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

    public static void teleportToSavePosition(LivingEntity entity) {
        long lastBlockPos = entity.getPersistentData().getLong(ModConstants.LAST_BLOCK_POS);
        var teleportPos = BlockPos.of(lastBlockPos);

        var positionInRadius = positionInRadius(entity, teleportPos);
        System.out.println(positionInRadius);
        if (positionInRadius == null) {
            entity.teleportTo(teleportPos.getX(), entity.level.getMaxBuildHeight() + VoidTotemConfig.COMMON_CONFIG.TELEPORT_HEIGHT_OFFSET.get(), teleportPos.getZ());
            if (entity instanceof ServerPlayer player)
            player.connection.aboveGroundTickCount = 0;
        }
    }

    public static BlockPos positionInRadius(LivingEntity entity, BlockPos teleportPos) {
        for (int i = 0; i < 16; i++) {

            var maxBuildHeight = entity.level.getMaxBuildHeight();

            var x = teleportPos.getX() + (entity.getRandom().nextDouble() - 0.5D) * 4.0D;
            var y = Mth.clamp(entity.getRandom().nextInt() * maxBuildHeight, 0.0D, maxBuildHeight - 1);
            var z = teleportPos.getZ() + (entity.getRandom().nextDouble() - 0.5D) * 4.0;

            var pos = new BlockPos(x, y, z);
            if (entity.randomTeleport(x, y, z, true)) {
                return pos;
            }
        }
        return null;
    }

    public static void sendTotemEffectPacket(ItemStack stack, LivingEntity entity) {
        if (entity instanceof ServerPlayer player)
        PacketHandler.sendToPlayer(new TotemEffectPacket(stack, entity), player);
        PacketHandler.sendToAllTracking(new TotemEffectPacket(stack, entity), entity);
    }
}