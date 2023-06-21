package com.affehund.voidtotem;

import com.affehund.voidtotem.api.VoidTotemEventCallback;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public class FabricPlatformHelper implements PlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public InteractionResult getVoidTotemEventResult(ItemStack itemStack, LivingEntity livingEntity, DamageSource source) {
        return VoidTotemEventCallback.EVENT.invoker().interact(itemStack, livingEntity, source);
    }

    @Override
    public ItemStack getTotemFromAdditionalSlot(LivingEntity livingEntity, Predicate<ItemStack> predicate) {
        if (isModLoaded(ModConstants.TRINKETS_MOD_ID)) {
            return TrinketsApi.getTrinketComponent(livingEntity).map(component -> {
                List<Tuple<SlotReference, ItemStack>> res = component.getEquipped(predicate);
                return res.size() > 0 ? res.get(0).getB() : null;
            }).orElse(null);
        }
        return null;
    }

    @Override
    public Item getVoidTotemItem() {
        return VoidTotemFabric.VOID_TOTEM_ITEM;
    }

    @Override
    public void sendTotemEffectPacket(ItemStack itemStack, LivingEntity livingEntity) {
        var buf = PacketByteBufs.create();
        buf.writeItem(itemStack);
        buf.writeInt(livingEntity.getId());
        if (livingEntity instanceof ServerPlayer player) {
            ServerPlayNetworking.send(player, ModConstants.TOTEM_EFFECT_PACKET_LOCATION, buf);
        }
        for (ServerPlayer player : PlayerLookup.tracking((ServerLevel) livingEntity.level(), livingEntity.blockPosition())) {
            ServerPlayNetworking.send(player, ModConstants.TOTEM_EFFECT_PACKET_LOCATION, buf);
        }
    }

    @Override
    public ParticleOptions getVoidTotemParticle() {
        return VoidTotemFabric.VOID_TOTEM_PARTICLE;
    }

    @Override
    public List<? extends String> getBlacklistedDimensions() {
        return VoidTotemFabric.CONFIG.BLACKLISTED_DIMENSIONS;
    }

    @Override
    public boolean giveTotemEffects() {
        return VoidTotemFabric.CONFIG.GIVE_TOTEM_EFFECTS;
    }

    @Override
    public boolean isInvertedBlacklist() {
        return VoidTotemFabric.CONFIG.IS_INVERTED_BLACKLIST;
    }

    @Override
    public boolean needsTotem() {
        return VoidTotemFabric.CONFIG.NEEDS_TOTEM;
    }

    @Override
    public boolean showTotemTooltip() {
        return VoidTotemFabric.CONFIG.SHOW_TOTEM_TOOLTIP;
    }

    @Override
    public int teleportHeightOffset() {
        return VoidTotemFabric.CONFIG.TELEPORT_HEIGHT_OFFSET;
    }

    @Override
    public boolean useTotemFromInventory() {
        return VoidTotemFabric.CONFIG.USE_TOTEM_FROM_INVENTORY;
    }
}
