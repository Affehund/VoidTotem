package com.affehund.voidtotem.core;

import com.affehund.voidtotem.VoidTotem;
import com.affehund.voidtotem.api.ModEvents;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ModUtils {
    public static boolean tryUseVoidTotem(LivingEntity livingEntity, DamageSource source) {
        if (!isDimensionBlacklisted(livingEntity) && isOutOfWorld(source, livingEntity)) {
            if (hasRequestedTeleportPos(livingEntity)) return false;
            var stack = getTotemItemStack(livingEntity);

            if (stack != null) {

                var result = ModEvents.VOID_TOTEM_EVENT.invoker().interact(stack, livingEntity, source);
                if (result.equals(ActionResult.CONSUME)) return false;
                if (result.equals(ActionResult.CONSUME_PARTIAL)) return true;

                if (livingEntity instanceof ServerPlayerEntity player) {
                    player.networkHandler.floatingTicks = 0;
                    giveUseStatAndCriterion(stack, player);
                }

                stack = damageOrShrinkItemStack(stack, livingEntity);

                if (livingEntity.hasPassengers()) livingEntity.removeAllPassengers();

                livingEntity.stopRiding();
                livingEntity.setHealth(1.0f);
                ((LivingEntityAccessor) livingEntity).setFallDamageImmune(true);

                teleportToSavePosition(livingEntity);
                sendTotemEffectPacket(stack, livingEntity);

                return true;
            }
        }
        return false;
    }

    public static boolean isDimensionBlacklisted(LivingEntity livingEntity) {
        return VoidTotem.CONFIG.BLACKLISTED_DIMENSIONS.contains(livingEntity.world.getRegistryKey().getValue().toString());
    }

    public static boolean isOutOfWorld(DamageSource source, LivingEntity livingEntity) {
        return source.equals(DamageSource.OUT_OF_WORLD) && livingEntity.getY() < livingEntity.world.getBottomY();
    }

    public static boolean hasRequestedTeleportPos(LivingEntity livingEntity) {
        return livingEntity instanceof ServerPlayerEntity player && player.networkHandler.requestedTeleportPos != null;
    }

    public static ItemStack getTotemItemStack(LivingEntity livingEntity) {
        if (VoidTotem.CONFIG.NEEDS_TOTEM) {
            var possibleTotemStacks = filterPossibleTotemStacks(getTotemFromTrinkets(livingEntity), getTotemFromInventory(livingEntity), getTotemFromHands(livingEntity));
            return possibleTotemStacks.stream().findFirst().orElse(null);
        }
        return ItemStack.EMPTY;
    }

    public static List<ItemStack> filterPossibleTotemStacks(ItemStack... stacks) {
        return Arrays.stream(stacks).filter(Objects::nonNull).toList();
    }

    public static ItemStack getTotemFromTrinkets(LivingEntity livingEntity) {
        if (FabricLoader.getInstance().isModLoaded(ModConstants.TRINKETS_MOD_ID)) {
            return ModUtils.findTrinketsItem(livingEntity, ModUtils::isVoidTotemOrAdditionalTotem);
        }
        return null;
    }

    public static ItemStack findTrinketsItem(LivingEntity livingEntity, Predicate<ItemStack> filter) {
        return TrinketsApi.getTrinketComponent(livingEntity).map(component -> {
            List<Pair<SlotReference, ItemStack>> res = component.getEquipped(filter);
            return res.size() > 0 ? res.get(0).getRight() : null;
        }).orElse(null);
    }

    public static ItemStack getTotemFromInventory(LivingEntity livingEntity) {
        if (VoidTotem.CONFIG.USE_TOTEM_FROM_INVENTORY && livingEntity instanceof ServerPlayerEntity player) {
            for (var stack : player.getInventory().main) {
                if (ModUtils.isVoidTotemOrAdditionalTotem(stack)) return stack;
            }
        }
        return null;
    }

    public static ItemStack getTotemFromHands(LivingEntity livingEntity) {
        for (var hand : Hand.values()) {
            var stack = livingEntity.getStackInHand(hand);
            if (ModUtils.isVoidTotemOrAdditionalTotem(stack)) return stack;
        }
        return null;
    }

    public static boolean isVoidTotemOrAdditionalTotem(ItemStack stack) {
        return isVoidTotem(stack) || isAdditionalTotem(stack);
    }

    public static boolean isVoidTotem(ItemStack stack) {
        return stack.getItem().equals(VoidTotem.VOID_TOTEM_ITEM);
    }

    public static boolean isAdditionalTotem(ItemStack stack) {
        return stack.isIn(ModConstants.ADDITIONAL_TOTEMS_TAG);
    }

    public static ItemStack damageOrShrinkItemStack(ItemStack stack, LivingEntity livingEntity) {
        var copiedStack = stack.copy();
        if (stack.isDamageable()) {
            stack.damage(1, livingEntity, e -> e.sendToolBreakStatus(livingEntity.getActiveHand()));
        } else {
            stack.decrement(1);
        }
        return copiedStack;
    }

    public static void giveUseStatAndCriterion(ItemStack stack, ServerPlayerEntity player) {
        if (!stack.isEmpty()) {
            player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            Criteria.USED_TOTEM.trigger(player, stack);
        }
    }

    public static void teleportToSavePosition(LivingEntity livingEntity) {
        var lastPos = BlockPos.fromLong(((LivingEntityAccessor) livingEntity).getLastSaveBlockPosAsLong());

        var positionInRadius = positionNearby(livingEntity, lastPos);
        if (positionInRadius == null) {
            livingEntity.teleport(lastPos.getX(), livingEntity.world.getTopY() + VoidTotem.CONFIG.TELEPORT_HEIGHT_OFFSET, lastPos.getZ());
            if (livingEntity instanceof ServerPlayerEntity player) player.networkHandler.floatingTicks = 0;
        }
    }

    public static BlockPos positionNearby(LivingEntity livingEntity, BlockPos lastPos) {
        BlockPos teleportPos = null;
        for (int i = 0; i < 16; i++) {

            var world = livingEntity.world;
            var x = lastPos.getX() + (livingEntity.getRandom().nextDouble() - 0.5D) * 16.0D;
            var y = MathHelper.clamp(lastPos.getY() + (double) (livingEntity.getRandom().nextInt(16) - 8), world.getBottomY(), world.getTopY() - 1);
            var z = lastPos.getZ() + (livingEntity.getRandom().nextDouble() - 0.5D) * 16.0D;

            var pos = new BlockPos(x, y, z);
            if (livingEntity.teleport(x, y, z, true)) {
                teleportPos = pos;
                break;
            }

        }
        return teleportPos;
    }

    public static void sendTotemEffectPacket(ItemStack stack, LivingEntity livingEntity) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeItemStack(stack);
        buf.writeInt(livingEntity.getId());
        if (livingEntity instanceof ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, ModConstants.IDENTIFIER_TOTEM_EFFECT_PACKET, buf);
        }
        for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) livingEntity.world, livingEntity.getBlockPos())) {
            ServerPlayNetworking.send(player, ModConstants.IDENTIFIER_TOTEM_EFFECT_PACKET, buf);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void playActivateAnimation(ItemStack stack, Entity entity) {
        var mc = MinecraftClient.getInstance();
        mc.particleManager.addEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);

        if (mc.world != null)
            mc.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_TOTEM_USE, entity.getSoundCategory(), 1.0F, 1.0F, false);

        if (entity == mc.player)
            mc.gameRenderer.showFloatingItem(stack);
    }

    public static void setLastSaveBlockPos(LivingEntity livingEntity) {
        var world = livingEntity.world;
        var currentPos = livingEntity.getBlockPos();
        var lastPos = BlockPos.fromLong(((LivingEntityAccessor) livingEntity).getLastSaveBlockPosAsLong());
        if (isSaveBlockPos(world, currentPos.down())) {
            if (!lastPos.equals(currentPos) || !isSaveBlockPos(world, lastPos.down())) {
                ((LivingEntityAccessor) livingEntity).setLastSaveBlockPosAsLong(currentPos.asLong());
            }
        }
    }

    public static boolean isSaveBlockPos(World world, BlockPos pos) {
        var state = world.getBlockState(pos);
        return state.isSolidBlock(world, pos);
    }

    public static void resetFallDamageImmunity(LivingEntity livingEntity) {
        if (((LivingEntityAccessor) livingEntity).isFallDamageImmune()) {
            var canPlayerFly = false;
            if (livingEntity instanceof ServerPlayerEntity player) {
                player.networkHandler.floatingTicks = 0;
                var abilities = player.getAbilities();
                if (abilities.flying || abilities.allowFlying)
                    canPlayerFly = true;
            }

            if (livingEntity.isSubmergedInWater() || livingEntity.world.getBlockState(livingEntity.getBlockPos()).getBlock().equals(Blocks.COBWEB) || canPlayerFly) {
                ((LivingEntityAccessor) livingEntity).setFallDamageImmune(false);
            }
        }
    }
}
