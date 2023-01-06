package com.affehund.voidtotem;

import com.affehund.voidtotem.core.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ServiceLoader;

public class VoidTotem {

    public static final Logger LOGGER = LoggerFactory.getLogger(ModConstants.MOD_NAME);

    public static final PlatformHelper PLATFORM = loadPlatform();

    private static <T> T loadPlatform() {
        var loadedService = ServiceLoader.load((Class<T>) PlatformHelper.class)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + PlatformHelper.class.getName()));
        return loadedService;
    }


    public static void init() {
        VoidTotem.LOGGER.info("Loading up {} ({})", ModConstants.MOD_NAME, PLATFORM.getPlatformName());
    }

    public static void onItemTooltip(ItemStack itemStack, List<Component> tooltip) {
        if (ModUtils.isVoidTotemOrAdditionalTotem(itemStack) && PLATFORM.showTotemTooltip()) {
            tooltip.add(Component.translatable(ModConstants.TOOLTIP_VOID_TOTEM).withStyle(ChatFormatting.GRAY));
        }
    }
}