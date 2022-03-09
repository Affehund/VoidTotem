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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ModUtils {
    public static boolean tryUseVoidTotem(LivingEntity livingEntity, DamageSource source) {
        if (!isDimensionBlacklisted(livingEntity) && isOutOfWorld(source, livingEntity)) {
            if (livingEntity instanceof ServerPlayerEntity player && player.networkHandler.requestedTeleportPos == null) {
                player.networkHandler.floatingTicks = 0;

                ItemStack stack = getTotemItemStack(player);

                if (stack != null) {
                    ActionResult result = ModEvents.VOID_TOTEM_EVENT.invoker().interact(stack, livingEntity, source);
                    if (result.equals(ActionResult.FAIL)) return true;
                    giveUseStatAndCriterion(stack, player);
                    stack = damageOrShrinkItemStack(stack, player);

                    if (player.hasPlayerRider()) player.removeAllPassengers();

                    player.stopRiding();
                    player.addScoreboardTag(ModConstants.NBT_TAG);
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
        return VoidTotem.CONFIG.BLACKLISTED_DIMENSIONS.contains(livingEntity.world.getRegistryKey().getValue().toString());
    }

    public static boolean isOutOfWorld(DamageSource source, LivingEntity livingEntity) {
        return source.equals(DamageSource.OUT_OF_WORLD) && livingEntity.getY() < livingEntity.world.getBottomY();
    }

    public static ItemStack getTotemItemStack(ServerPlayerEntity player) {
        if (VoidTotem.CONFIG.NEEDS_TOTEM) {
            var possibleTotemStacks = filterPossibleTotemStacks(getTotemFromTrinkets(player), getTotemFromInventory(player), getTotemFromHands(player));
            return possibleTotemStacks.stream().findFirst().orElse(null);
        }
        return ItemStack.EMPTY;
    }

    public static List<ItemStack> filterPossibleTotemStacks(ItemStack... stacks) {
        return Arrays.stream(stacks).filter(Objects::nonNull).toList();
    }

    public static ItemStack findTrinketsItem(ServerPlayerEntity player, Predicate<ItemStack> filter) {
        return TrinketsApi.getTrinketComponent(player).map(component -> {
            List<Pair<SlotReference, ItemStack>> res = component.getEquipped(filter);
            return res.size() > 0 ? res.get(0).getRight() : ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
    }

    public static ItemStack getTotemFromTrinkets(ServerPlayerEntity player) {
        if (FabricLoader.getInstance().isModLoaded(ModConstants.TRINKETS_MOD_ID)) {
            return ModUtils.findTrinketsItem(player, stack -> stack.isIn(ModConstants.ADDITIONAL_TOTEMS_TAG) || stack.isOf(VoidTotem.VOID_TOTEM_ITEM));
        }
        return null;
    }

    public static ItemStack getTotemFromInventory(ServerPlayerEntity player) {
        if (VoidTotem.CONFIG.USE_TOTEM_FROM_INVENTORY) {
            for (ItemStack stack : player.getInventory().main) {
                if (ModUtils.isVoidTotemOrAdditionalTotem(stack)) return stack;
            }
        }
        return null;
    }

    public static ItemStack getTotemFromHands(ServerPlayerEntity player) {
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
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

    public static ItemStack damageOrShrinkItemStack(ItemStack stack, ServerPlayerEntity player) {
        var copiedStack = stack.copy();
        if (stack.isDamageable()) {
            stack.damage(1, player.getRandom(), player);
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

    public static void teleportToSavePosition(ServerPlayerEntity player) {
        long lastBlockPos = ((IPlayerEntityMixinAccessor) player).getBlockPosAsLong();
        var teleportPos = BlockPos.fromLong(lastBlockPos);

        var positionInRadius = positionInRadius(player, teleportPos);
        if (positionInRadius == null) {
            player.teleport(teleportPos.getX(), player.world.getTopY() + VoidTotem.CONFIG.TELEPORT_HEIGHT_OFFSET, teleportPos.getZ());
            player.networkHandler.floatingTicks = 0;
        }
    }

    public static BlockPos positionInRadius(ServerPlayerEntity player, BlockPos teleportPos) {
        for (int i = 0; i < 16; i++) {

            var maxBuildHeight = player.world.getTopY();

            var x = teleportPos.getX() + (player.getRandom().nextDouble() - 0.5D) * 4.0D;
            var y = MathHelper.clamp(player.getRandom().nextInt() * maxBuildHeight, 0.0D, maxBuildHeight - 1);
            var z = teleportPos.getZ() + (player.getRandom().nextDouble() - 0.5D) * 4.0;

            var pos = new BlockPos(x, y, z);
            if (player.teleport(x, y, z, true)) {
                return pos;
            }
        }
        return null;
    }

    public static void sendTotemEffectPacket(ItemStack stack, ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeItemStack(stack);
        buf.writeInt(player.getId());
        ServerPlayNetworking.send(player, ModConstants.IDENTIFIER_TOTEM_EFFECT_PACKET, buf);
        for (ServerPlayerEntity player2 : PlayerLookup.tracking((ServerWorld) player.world,
                player.getBlockPos())) {
            ServerPlayNetworking.send(player2, ModConstants.IDENTIFIER_TOTEM_EFFECT_PACKET, buf);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void playActivateAnimation(ItemStack stack, Entity entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.particleManager.addEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);

        if (mc.world != null)
            mc.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_TOTEM_USE, entity.getSoundCategory(), 1.0F, 1.0F, false);

        if (entity == mc.player)
            mc.gameRenderer.showFloatingItem(stack);
    }
}
