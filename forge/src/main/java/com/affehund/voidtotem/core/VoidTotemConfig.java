package com.affehund.voidtotem.core;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class VoidTotemConfig {

    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupCommonConfig(configBuilder);
        COMMON_SPEC = configBuilder.build();
    }

    public static ForgeConfigSpec.BooleanValue ADD_END_CITY_TREASURE;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_DIMENSIONS;
    public static ForgeConfigSpec.BooleanValue DISPLAY_TOTEM_ON_CHEST;
    public static ForgeConfigSpec.BooleanValue GIVE_TOTEM_EFFECTS;
    public static ForgeConfigSpec.BooleanValue IS_INVERTED_BLACKLIST;
    public static ForgeConfigSpec.BooleanValue NEEDS_TOTEM;
    public static ForgeConfigSpec.BooleanValue SHOW_TOTEM_TOOLTIP;
    public static ForgeConfigSpec.IntValue TELEPORT_HEIGHT_OFFSET;
    public static ForgeConfigSpec.BooleanValue USE_TOTEM_FROM_INVENTORY;

    private static void setupCommonConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Void Totem Common Config");

        ADD_END_CITY_TREASURE = builder
                .comment("Whether the void totem should be added to the end city treasure.")
                .define("add_end_city_treasure", true);

        BLACKLISTED_DIMENSIONS = builder
                .comment("Dimensions in this blacklist will prevent the functionality of the void totem. Example: \"minecraft:overworld\"")
                .defineList("blacklisted_dimensions",
                        new ArrayList<>(), entry -> true);

        DISPLAY_TOTEM_ON_CHEST = builder
                .comment("Whether the void totem is displayed on the chest when worn in the curios charm slot (curios mod must be installed).")
                .define("display_totem_on_chest", true);

        GIVE_TOTEM_EFFECTS = builder
                .comment("Whether you get the regeneration and absorption effect on the void totem execution. This also removes all previous effects.")
                .define("give_totem_effects", false);

        IS_INVERTED_BLACKLIST = builder
                .comment("Whether the blacklist is inverted, meaning the void totem only works in whitelisted dimensions.")
                .define("is_inverted_blacklist", false);

        NEEDS_TOTEM = builder
                .comment("Whether you need a totem to prevent death when falling into the void.")
                .define("needs_totem", true);

        SHOW_TOTEM_TOOLTIP = builder
                .comment("This sets whether a tooltip is shown on the totem.")
                .define("show_totem_tooltip", true);

        TELEPORT_HEIGHT_OFFSET = builder
                .comment("The height offset from the world height you will be teleported if you can't be placed on a block.")
                .defineInRange("teleport_height_offset", 64, 0, 1024);

        USE_TOTEM_FROM_INVENTORY = builder
                .comment("Whether you can use the void totem from anywhere in your inventory.")
                .define("use_totem_from_inventory", false);
    }
}
