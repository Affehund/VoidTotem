package com.affehund.voidtotem.core;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;

import com.affehund.voidtotem.ModConstants;
import com.affehund.voidtotem.VoidTotem;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Bus.MOD)
public class VoidTotemConfig {
	public static class VoidCommonConfig {
		public final BooleanValue ALLOW_TOTEM_OF_UNDYING;
		public final ConfigValue<ArrayList<String>> BLACKLISTED_DIMENSIONS;
		public final BooleanValue NEEDS_TOTEM;
		public final IntValue TELEPORT_HEIGHT;

		public VoidCommonConfig(ForgeConfigSpec.Builder builder) {
			builder.comment("Void Totem Common Config").push("general");
			ALLOW_TOTEM_OF_UNDYING = builder
					.comment("This sets whether the totem of undying will prevent death when falling into the void.")
					.define("allow_totem_of_undying", false);
			BLACKLISTED_DIMENSIONS = builder.comment(
					"This adds dimensions to a blacklist where you die if you fall into the void. Example: \"minecraft:overworld\".")
					.define("blacklisted_dimensions", new ArrayList<String>());
			NEEDS_TOTEM = builder
					.comment("This sets whether you need a totem to prevent death when falling into the void.")
					.define("needs_totem", true);
			TELEPORT_HEIGHT = builder
					.comment("This sets the height you will be teleported when you can't be placed on a block.")
					.defineInRange("teleport_height", 320, 256, 2048);
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

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading event) {
		VoidTotem.LOGGER.info("Loaded {} config file from {}", event.getConfig().getFileName(), ModConstants.MOD_ID);
	}
}
