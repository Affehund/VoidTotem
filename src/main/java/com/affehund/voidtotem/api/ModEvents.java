package com.affehund.voidtotem.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public interface ModEvents {
    /**
     * Fail the event to cancel the default functionality.
     * Consume or partially consume the event for more options.
     */
    Event<VoidTotemEvent> VOID_TOTEM_EVENT = EventFactory.createArrayBacked(VoidTotemEvent.class,
            (listeners) -> (stack, entity, source) -> {
                for (VoidTotemEvent listener : listeners) {
                    ActionResult result = listener.interact(stack, entity, source);
                    if(result.equals(ActionResult.FAIL) {
                        return ActionResult.CONSUME_PARTIAL; // old functionality
                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });
    interface VoidTotemEvent {
        ActionResult interact(ItemStack stack, LivingEntity entity, DamageSource source);
    }
}
