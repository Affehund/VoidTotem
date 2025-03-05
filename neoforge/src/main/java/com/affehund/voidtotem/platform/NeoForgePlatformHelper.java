package com.affehund.voidtotem.platform;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.VoidTotemForge;
import com.affehund.voidtotem.config.VoidTotemCommonConfig;
import com.affehund.voidtotem.network.TotemEffectPacket;
import com.affehund.voidtotem.platform.services.IPlatformHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;
import java.util.function.Predicate;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Item getVoidTotemItem() {
        return VoidTotemForge.VOID_TOTEM_ITEM.get();
    }

    @Override
    public ParticleOptions getVoidTotemParticleOptions() {
        return VoidTotemForge.VOID_TOTEM_PARTICLE.get();
    }

    @Override
    public ItemStack getTotemFromAdditionalSlot(LivingEntity livingEntity, Predicate<ItemStack> filter) {
        if (isModLoaded(ModConstants.CURIOS_MOD_ID)) {
            return CuriosApi.getCuriosInventory(livingEntity).map(
                    inv -> inv.findFirstCurio(filter)
                            .map(SlotResult::stack).orElse(null)).orElse(null);
        }
        return null;
    }

    @Override
    public List<? extends String> getBlocklistedDimensions() {
        return VoidTotemCommonConfig.BLOCKLISTED_DIMENSIONS.get();
    }

    @Override
    public boolean isInvertedBlocklist() {
        return VoidTotemCommonConfig.IS_INVERTED_BLOCKLIST.get();
    }

    @Override
    public int teleportHeightOffset() {
        return VoidTotemCommonConfig.TELEPORT_HEIGHT_OFFSET.get();
    }

    @Override
    public boolean useTotemFromInventory() {
        return VoidTotemCommonConfig.USE_TOTEM_FROM_INVENTORY.get();
    }

    @Override
    public void sendTotemEffectPacket(ItemStack itemStack, LivingEntity livingEntity) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(livingEntity, new TotemEffectPacket(itemStack, livingEntity.getId()));
    }
}