package com.affehund.voidtotem;

import com.affehund.voidtotem.api.VoidTotemEvent;
import com.affehund.voidtotem.core.VoidTotemConfig;
import com.affehund.voidtotem.core.network.PacketHandler;
import com.affehund.voidtotem.core.network.TotemEffectPacket;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;
import java.util.function.Predicate;

public class ForgePlatformHelper implements PlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public InteractionResult getVoidTotemEventResult(ItemStack itemStack, LivingEntity livingEntity, DamageSource source) {
        var event = new VoidTotemEvent(itemStack, livingEntity, source);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult().equals(Event.Result.ALLOW)) return InteractionResult.CONSUME_PARTIAL;
        if (event.getResult().equals(Event.Result.DENY)) return InteractionResult.CONSUME;
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack getTotemFromAdditionalSlot(LivingEntity livingEntity, Predicate<ItemStack> filter) {
        if (isModLoaded(ModConstants.CURIOS_MOD_ID)) {
            return CuriosApi.getCuriosHelper().findFirstCurio(livingEntity, filter).map(SlotResult::stack).orElse(null);
        }
        return null;
    }

    @Override
    public Item getVoidTotemItem() {
        return VoidTotemForge.VOID_TOTEM_ITEM.get();
    }

    @Override
    public void sendTotemEffectPacket(ItemStack itemStack, LivingEntity livingEntity) {
        if (livingEntity instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(new TotemEffectPacket(itemStack, player), player);
        }
        PacketHandler.sendToAllTracking(new TotemEffectPacket(itemStack, livingEntity), livingEntity);
    }

    @Override
    public ParticleOptions getVoidTotemParticle() {
        return VoidTotemForge.VOID_TOTEM_PARTICLE.get();
    }

    @Override
    public List<? extends String> getBlacklistedDimensions() {
        return VoidTotemConfig.BLACKLISTED_DIMENSIONS.get();
    }

    @Override
    public boolean giveTotemEffects() {
        return VoidTotemConfig.GIVE_TOTEM_EFFECTS.get();
    }

    @Override
    public boolean isInvertedBlacklist() {
        return VoidTotemConfig.IS_INVERTED_BLACKLIST.get();
    }

    @Override
    public boolean needsTotem() {
        return VoidTotemConfig.NEEDS_TOTEM.get();
    }

    @Override
    public boolean showTotemTooltip() {
        return VoidTotemConfig.SHOW_TOTEM_TOOLTIP.get();
    }

    @Override
    public int teleportHeightOffset() {
        return VoidTotemConfig.TELEPORT_HEIGHT_OFFSET.get();
    }

    @Override
    public boolean useTotemFromInventory() {
        return VoidTotemConfig.USE_TOTEM_FROM_INVENTORY.get();
    }
}
