package com.affehund.voidtotem.core;

import com.affehund.voidtotem.ModConstants;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VoidTotemConfig {
    public static class VoidCommonConfig {
        public final ForgeConfigSpec.BooleanValue ADD_END_CITY_TREASURE;
        public final ConfigValue<ArrayList<String>> BLACKLISTED_DIMENSIONS;
        public final BooleanValue NEEDS_TOTEM;
        public final BooleanValue SHOW_TOTEM_TOOLTIP;
        public final IntValue TELEPORT_HEIGHT_OFFSET;
        public final BooleanValue USE_TOTEM_FROM_INVENTORY;

        public VoidCommonConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Void Totem Common Config").push("general");
            ADD_END_CITY_TREASURE = builder
                    .comment("This sets whether the void totem will be added to the end city treasure.")
                    .define("add_end_city_treasure", true);
            BLACKLISTED_DIMENSIONS = builder
                    .comment("This adds dimensions to a blacklist where you die if you fall into the void. Example: \"minecraft:overworld\".")
                    .define("blacklisted_dimensions",
                            new ArrayList<>());
            NEEDS_TOTEM = builder
                    .comment("This sets whether you need a totem to prevent death when falling into the void.")
                    .define("needs_totem", true);
            SHOW_TOTEM_TOOLTIP = builder
                    .comment("This sets whether a tooltip is shown on the totem.")
                    .define("show_totem_tooltip", true);
            TELEPORT_HEIGHT_OFFSET = builder
                    .comment("This sets the height offset you will be teleported when you can't be placed on a block.")
                    .defineInRange("teleport_height_offset", 64, 0, 1024);
            USE_TOTEM_FROM_INVENTORY = builder
                    .comment("This sets whether you can use a totem from anywhere in your inventory.")
                    .define("use_totem_from_inventory", false);
            builder.pop();
        }
    }

    public static final ForgeConfigSpec COMMON_CONFIG_SPEC;
    public static final VoidCommonConfig COMMON_CONFIG;

    static {
        final Pair<VoidCommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
                .configure(VoidCommonConfig::new);
        COMMON_CONFIG_SPEC = specPair.getRight();
        COMMON_CONFIG = specPair.getLeft();
    }
}
