package com.affehund.voidtotem.core;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class VoidTotemCommonConfig {

    public static final ForgeConfigSpec SPEC;

    static {
        var configBuilder = new ForgeConfigSpec.Builder();
        setupCommonConfig(configBuilder);
        SPEC = configBuilder.build();
    }

    public static ForgeConfigSpec.BooleanValue ADD_END_CITY_TREASURE;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_DIMENSIONS;
    public static ForgeConfigSpec.BooleanValue GIVE_TOTEM_EFFECTS;
    public static ForgeConfigSpec.BooleanValue IS_INVERTED_BLACKLIST;
    public static ForgeConfigSpec.BooleanValue NEEDS_TOTEM;
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

        GIVE_TOTEM_EFFECTS = builder
                .comment("Whether you get the regeneration and absorption effect on the void totem execution. This also removes all previous effects.")
                .define("give_totem_effects", true);

        IS_INVERTED_BLACKLIST = builder
                .comment("Whether the blacklist is inverted, meaning the void totem only works in whitelisted dimensions.")
                .define("is_inverted_blacklist", false);

        NEEDS_TOTEM = builder
                .comment("Whether you need a totem to prevent death when falling into the void.")
                .define("needs_totem", true);

        TELEPORT_HEIGHT_OFFSET = builder
                .comment("The height offset from the world height you will be teleported if you can't be placed on a block.")
                .defineInRange("teleport_height_offset", 64, 0, 1024);

        USE_TOTEM_FROM_INVENTORY = builder
                .comment("Whether you can use the void totem from anywhere in your inventory.")
                .define("use_totem_from_inventory", false);
    }
}
