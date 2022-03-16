package com.affehund.voidtotem.api;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;


/**
 * Cancel the event to disable normal functionality
 */
@Cancelable
@Event.HasResult
public class VoidTotemEvent extends Event {

    private final ItemStack itemStack;
    private final LivingEntity entity;
    private final DamageSource source;

    public VoidTotemEvent(ItemStack itemStack, LivingEntity entity, DamageSource source) {
        this.itemStack = itemStack;
        this.entity = entity;
        this.source = source;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public DamageSource getSource() {
        return source;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    @Override
    public void setResult(Result value) {
        if (value != Result.DEFAULT) setCanceled(true);
        super.setResult(value);
    }
}
