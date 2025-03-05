package com.affehund.voidtotem.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class VoidTotemCommonConfig {

    public static final ModConfigSpec SPEC;

    static {
        var configBuilder = new ModConfigSpec.Builder();
        setupCommonConfig(configBuilder);
        SPEC = configBuilder.build();
    }
    
    public static ModConfigSpec.ConfigValue<List<? extends String>> BLOCKLISTED_DIMENSIONS;
    public static ModConfigSpec.BooleanValue IS_INVERTED_BLOCKLIST;
    public static ModConfigSpec.IntValue TELEPORT_HEIGHT_OFFSET;
    public static ModConfigSpec.BooleanValue USE_TOTEM_FROM_INVENTORY;

    private static void setupCommonConfig(ModConfigSpec.Builder builder) {
        builder.comment("Void Totem Common Config");

        BLOCKLISTED_DIMENSIONS = builder
                .comment("Dimensions in this blocklist will prevent the functionality of the void totem. Example: \"minecraft:overworld\"")
                .defineList("blocklisted_dimensions",
                        new ArrayList<>(), entry -> true);

        IS_INVERTED_BLOCKLIST = builder
                .comment("Whether the blocklist is inverted, meaning the void totem only works in allowlisted dimensions.")
                .define("is_inverted_blocklist", false);


        TELEPORT_HEIGHT_OFFSET = builder
                .comment("The height offset from the world height you will be teleported if you can't be placed on a block.")
                .defineInRange("teleport_height_offset", 64, 0, 1024);

        USE_TOTEM_FROM_INVENTORY = builder
                .comment("Whether you can use the void totem from anywhere in your inventory.")
                .define("use_totem_from_inventory", false);
    }
}
