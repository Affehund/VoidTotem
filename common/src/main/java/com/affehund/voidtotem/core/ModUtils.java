package com.affehund.voidtotem.core;

import com.affehund.voidtotem.mixin.ServerGamePacketListenerImplAccessor;
import com.affehund.voidtotem.platform.Services;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ModUtils {
    public static boolean isModLoaded(String modId) {
        return Services.PLATFORM.isModLoaded(modId);
    }

    public static boolean canProtectFromVoid(LivingEntity entity, DamageSource source) {
        String currentDim = entity.level().dimension().identifier().toString();
        boolean isBlocklisted = Services.PLATFORM.isInvertedBlocklist() != Services.PLATFORM.getBlocklistedDimensions().contains(currentDim);
        boolean isInVoid = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && entity.getY() < entity.level().getMinY();
        boolean isAwaitingPosition = entity instanceof ServerPlayer player && ((ServerGamePacketListenerImplAccessor) player.connection).getAwaitingPositionFromClient() != null;

        return !isBlocklisted && isInVoid && !isAwaitingPosition && hasVoidTotem(entity);
    }

    public static void handleVoidTotem(LivingEntity entity) {
        ItemStack stack = getTotemItemStack(entity);

        if (stack != null) {
            if (entity instanceof ServerPlayer player) {
                resetAboveGroundTickCount(player);

                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                CriteriaTriggers.USED_TOTEM.trigger(player, stack);
                player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }

            stack = shrinkItemStack(stack, entity);

            resetEntityState(entity);
            teleportToSavePosition(entity);

            Services.PLATFORM.sendTotemEffectPacket(stack, entity);
        }
    }

    public static void handleDefaultTotemActivation(LivingEntity entity) {
        ItemStack stack = getTotemItemStack(entity);

        if (stack != null) {
            stack = shrinkItemStack(stack, entity);

            if (entity instanceof ServerPlayer player) {
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                CriteriaTriggers.USED_TOTEM.trigger(player, new ItemStack(Items.TOTEM_OF_UNDYING)); // to give default totem advancement
                entity.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }

            entity.setHealth(1.0F);
            giveTotemEffects(entity, true);

            Services.PLATFORM.sendTotemEffectPacket(stack, entity);
        }
    }

    public static boolean hasVoidTotem(LivingEntity entity) {
        return getTotemItemStack(entity) != null;
    }

    public static void setLastSaveBlockPos(LivingEntity entity) {
        var level = entity.level();
        BlockPos currentPos = entity.blockPosition();
        BlockPos lastPos = BlockPos.of(((ILivingEntityMixin) entity).voidtotem$getLastSaveBlockPosAsLong());

        if (isSaveBlockPos(level, currentPos.below())) {
            if (!lastPos.equals(currentPos) || !isSaveBlockPos(level, lastPos.below())) {
                ((ILivingEntityMixin) entity).voidtotem$setLastSaveBlockPosAsLong(currentPos.asLong());
                ((ILivingEntityMixin) entity).voidtotem$setLastSaveBlockDim(entity.level().dimensionType());
            }
        }
    }

    public static void resetFallDamageImmunity(LivingEntity entity) {
        if (!((ILivingEntityMixin) entity).voidtotem$isFallDamageImmune()) return;

        boolean canFly = entity instanceof ServerPlayer player && (player.getAbilities().flying || player.getAbilities().mayfly);
        boolean isInWater = entity.isInWater();
        boolean isInCobweb = entity.level().getBlockState(entity.blockPosition()).is(Blocks.COBWEB);

        if (entity instanceof ServerPlayer player) {
            resetAboveGroundTickCount(player);
        }

        if (canFly || isInCobweb || isInWater) {
            ((ILivingEntityMixin) entity).voidtotem$setFallDamageImmune(false);
        }
    }

    public static void playActivateAnimation(ItemStack itemStack, int entityId) {
        Minecraft mc = Minecraft.getInstance();
        var level = mc.level;

        if (level != null) {
            Entity entity = mc.level.getEntity(entityId);

            if (entity != null) {
                mc.particleEngine.createTrackingEmitter(entity, Services.PLATFORM.getVoidTotemParticleOptions(), 30);
                level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);

                if (entity == mc.player) {
                    mc.gameRenderer.displayItemActivation(itemStack);
                }
            }
        }
    }

    private static void resetEntityState(LivingEntity entity) {
        entity.stopRiding();
        entity.ejectPassengers();
        entity.setHealth(1.0f);
        ((ILivingEntityMixin) entity).voidtotem$setFallDamageImmune(true);
        giveTotemEffects(entity, false);
    }


    private static ItemStack shrinkItemStack(ItemStack stack, LivingEntity entity) {
        ItemStack copiedStack = stack.copy();

        if (!(entity instanceof ServerPlayer player && player.isCreative())) {
            stack.shrink(1);
        }
        return copiedStack;
    }

    private static ItemStack getTotemItemStack(LivingEntity entity) {
        return Stream.of(getAdditionalTotems(entity), getTotemFromInventory(entity), getTotemFromHands(entity))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static ItemStack getAdditionalTotems(LivingEntity entity) {
        return Services.PLATFORM.getTotemFromAdditionalSlot(entity, ModUtils::isVoidTotemItem);
    }

    private static ItemStack getTotemFromInventory(LivingEntity entity) {
        if (Services.PLATFORM.useTotemFromInventory() && entity instanceof ServerPlayer player) {
            return player.getInventory().getNonEquipmentItems().stream()
                    .filter(ModUtils::isVoidTotemItem)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private static ItemStack getTotemFromHands(LivingEntity entity) {
        return Arrays.stream(InteractionHand.values())
                .map(entity::getItemInHand)
                .filter(ModUtils::isVoidTotemItem)
                .findFirst()
                .orElse(null);
    }

    private static void giveTotemEffects(LivingEntity entity, boolean giveFireRes) {
        entity.removeAllEffects();
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));

        if (giveFireRes) {
            entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
        }
    }

    private static boolean isVoidTotemItem(ItemStack stack) {
        return stack.getItem().equals(Services.PLATFORM.getVoidTotemItem());
    }

    private static void teleportToSavePosition(LivingEntity entity) {
        BlockPos lastPos = getNearLastPos(entity);
        if (lastPos != null) {
            entity.teleportTo(lastPos.getX(), lastPos.getY(), lastPos.getZ());
        } else {
            BlockPos currentPos = entity.blockPosition();
            entity.teleportTo(currentPos.getX(), entity.level().getMaxY() + Services.PLATFORM.teleportHeightOffset(), currentPos.getZ());
            if (entity instanceof ServerPlayer player) {
                resetAboveGroundTickCount(player);
            }
        }
    }

    private static BlockPos getNearLastPos(LivingEntity entity) {
        BlockPos lastPos = BlockPos.of(((ILivingEntityMixin) entity).voidtotem$getLastSaveBlockPosAsLong());
        var level = entity.level();

        if (((ILivingEntityMixin) entity).voidtotem$getLastSaveBlockDim() == level.dimensionType()) {
            return IntStream.range(0, 16)
                    .mapToObj(i -> {
                        double x = lastPos.getX() + (entity.getRandom().nextDouble() - 0.5) * 16.0;
                        double y = Mth.clamp(lastPos.getY() + entity.getRandom().nextInt(16) - 8, level.getMinY(), level.getMaxY() - 1);
                        double z = lastPos.getZ() + (entity.getRandom().nextDouble() - 0.5) * 16.0;
                        return new BlockPos((int) x, (int) y, (int) z);
                    })
                    .filter(pos -> entity.randomTeleport(pos.getX(), pos.getY(), pos.getZ(), false))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private static boolean isSaveBlockPos(@NotNull Level level, BlockPos pos) {
        return level.getBlockState(pos).isRedstoneConductor(level, pos);
    }

    private static void resetAboveGroundTickCount(ServerPlayer player) {
        ((ServerGamePacketListenerImplAccessor) player.connection).setAboveGroundTickCount(0);
    }
}
