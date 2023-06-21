package com.affehund.voidtotem.core;

import net.minecraftforge.common.ForgeConfigSpec;

public class VoidTotemClientConfig {

    public static final ForgeConfigSpec SPEC;

    static {
        var configBuilder = new ForgeConfigSpec.Builder();
        setupClientConfig(configBuilder);
        SPEC = configBuilder.build();
    }

    public static ForgeConfigSpec.BooleanValue DISPLAY_TOTEM_ON_CHEST;
    public static ForgeConfigSpec.BooleanValue SHOW_TOTEM_TOOLTIP;

    private static void setupClientConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Void Totem Client Config");

        DISPLAY_TOTEM_ON_CHEST = builder
                .comment("Whether the void totem is displayed on the chest when worn in the curios charm slot (curios mod must be installed).")
                .define("display_totem_on_chest", true);

        SHOW_TOTEM_TOOLTIP = builder
                .comment("This sets whether a tooltip is shown on the totem.")
                .define("show_totem_tooltip", true);
    }
}
