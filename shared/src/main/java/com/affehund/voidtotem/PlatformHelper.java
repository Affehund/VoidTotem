package com.affehund.voidtotem;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public interface PlatformHelper {
    String getPlatformName();

    boolean isModLoaded(String modId);

    InteractionResult getVoidTotemEventResult(ItemStack itemStack, LivingEntity livingEntity, DamageSource source);

    ItemStack getTotemFromAdditionalSlot(LivingEntity livingEntity, Predicate<ItemStack> filter);

    Item getVoidTotemItem();

    void sendTotemEffectPacket(ItemStack itemStack, LivingEntity livingEntity);

    /*
     * Config methods
     */
    List<? extends String> getBlacklistedDimensions();

    boolean giveTotemEffects();

    boolean isInvertedBlacklist();

    boolean needsTotem();

    boolean showTotemTooltip();

    int teleportHeightOffset();

    boolean useTotemFromInventory();
}
