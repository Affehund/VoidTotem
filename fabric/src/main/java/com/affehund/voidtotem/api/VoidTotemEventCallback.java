package com.affehund.voidtotem.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Fail the event to cancel the default functionality.
 * Consume or partially consume the event for more options.
 */
public interface VoidTotemEventCallback {

    Event<VoidTotemEventCallback> EVENT = EventFactory.createArrayBacked(VoidTotemEventCallback.class,
            (listeners) -> (itemStack, entity, source) -> {
                for (var listener : listeners) {
                    var result = listener.interact(itemStack, entity, source);

                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }

                return InteractionResult.PASS;
            });

    InteractionResult interact(ItemStack itemStack, LivingEntity entity, DamageSource source);
}