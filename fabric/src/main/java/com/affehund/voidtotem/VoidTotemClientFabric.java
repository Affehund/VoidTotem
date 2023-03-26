package com.affehund.voidtotem;

import com.affehund.voidtotem.core.ModUtils;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class VoidTotemClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((itemStack, context, tooltip) -> VoidTotem.onItemTooltip(itemStack, tooltip));

        registerVoidTotemPacket();
        registerVoidTotemParticle();

        if (ModUtils.isModLoaded(ModConstants.TRINKETS_MOD_ID) && VoidTotemFabric.CONFIG.DISPLAY_TOTEM_ON_CHEST) {
            renderVoidTotemTrinket();
        }

    }

    private void registerVoidTotemParticle() {
        ParticleFactoryRegistry.getInstance().register(VoidTotemFabric.VOID_TOTEM_PARTICLE, VoidTotemParticle.Provider::new);
    }

    private void registerVoidTotemPacket() {
        ClientPlayNetworking.registerGlobalReceiver(ModConstants.TOTEM_EFFECT_PACKET_LOCATION, (client, handler, buf, responseSender) -> {
            ItemStack itemStack = buf.readItem();
            assert client.level != null;
            Entity entity = client.level.getEntity(buf.readInt());
            client.execute(() -> ModUtils.playActivateAnimation(itemStack, entity));
        });
    }

    private void renderVoidTotemTrinket() {
        TrinketRendererRegistry.registerRenderer(VoidTotemFabric.VOID_TOTEM_ITEM,
                (itemStack, slotReference, contextModel, poseStack, multiBufferSource, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch) -> {
                    if (entity instanceof AbstractClientPlayer player) {
                        TrinketRenderer.translateToChest(poseStack,
                                (PlayerModel<AbstractClientPlayer>) contextModel, player);

                        poseStack.scale(0.35F, 0.35F, 0.35F);
                        poseStack.mulPose(Direction.DOWN.getRotation());

                        Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemDisplayContext.FIXED, 15728880, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, Minecraft.getInstance().level, 0);
                    }
                });
    }
}
