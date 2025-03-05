package com.affehund.voidtotem.integration;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.VoidTotemForge;
import com.affehund.voidtotem.core.ModUtils;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class CuriosCombatHandler {

    public static void registerCapabilities(final RegisterCapabilitiesEvent evt) {
        if (ModUtils.isModLoaded(ModConstants.CURIOS_MOD_ID)) {
            evt.registerItem(CuriosCapability.ITEM, (stack, ctx) -> new ICurio() {
                @Override
                public ItemStack getStack() {
                    return stack;
                }

                @Override
                public boolean canEquipFromUse(SlotContext ctx) {
                    return true;
                }
            }, VoidTotemForge.VOID_TOTEM_ITEM.get());
        }
    }
}
