package com.affehund.voidtotem.platform;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.VoidTotemFabric;
import com.affehund.voidtotem.network.TotemEffectPacket;
import com.affehund.voidtotem.platform.services.IPlatformHelper;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Item getVoidTotemItem() {
        return VoidTotemFabric.VOID_TOTEM_ITEM;
    }

    @Override
    public ParticleOptions getVoidTotemParticleOptions() {
        return VoidTotemFabric.VOID_TOTEM_PARTICLE;
    }

    @Override
    public ItemStack getTotemFromAdditionalSlot(LivingEntity livingEntity, Predicate<ItemStack> filter) {
        if (isModLoaded(ModConstants.TRINKETS_MOD_ID)) {
            return TrinketsApi.getTrinketComponent(livingEntity).map(component -> {
                List<Tuple<SlotReference, ItemStack>> res =
                        component.getEquipped(filter);
                return !res.isEmpty() ? res.getFirst().getB() : null;
            }).orElse(null);
        }
        return null;
    }

    @Override
    public List<? extends String> getBlocklistedDimensions() {
        return VoidTotemFabric.CONFIG.BLOCKLISTED_DIMENSIONS;
    }

    @Override
    public boolean isInvertedBlocklist() {
        return VoidTotemFabric.CONFIG.IS_INVERTED_BLOCKLIST;
    }

    @Override
    public int teleportHeightOffset() {
        return VoidTotemFabric.CONFIG.TELEPORT_HEIGHT_OFFSET;
    }

    @Override
    public boolean useTotemFromInventory() {
        return VoidTotemFabric.CONFIG.USE_TOTEM_FROM_INVENTORY;
    }

    @Override
    public void sendTotemEffectPacket(ItemStack itemStack, LivingEntity livingEntity) {
        TotemEffectPacket packet = new TotemEffectPacket(itemStack, livingEntity.getId());

        for (ServerPlayer serverPlayer : PlayerLookup.tracking(livingEntity)) {
            ServerPlayNetworking.send(serverPlayer, packet);
        }

        if (livingEntity instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, packet);
        }
    }
}
