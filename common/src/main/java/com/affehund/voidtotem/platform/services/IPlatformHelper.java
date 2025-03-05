package com.affehund.voidtotem.platform.services;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public interface IPlatformHelper {

    boolean isModLoaded(String modId);

    Item getVoidTotemItem();

    ParticleOptions getVoidTotemParticleOptions();

    ItemStack getTotemFromAdditionalSlot(LivingEntity livingEntity, Predicate<ItemStack> filter);

    List<? extends String> getBlocklistedDimensions();

    boolean isInvertedBlocklist();

    int teleportHeightOffset();

    boolean useTotemFromInventory();

    void sendTotemEffectPacket(ItemStack itemStack, LivingEntity livingEntity);
}