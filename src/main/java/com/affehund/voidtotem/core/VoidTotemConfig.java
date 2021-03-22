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

/**
 * A class for our config values (allow totem of undying, blacklisted
 * dimensions, teleport height, etc...).
 * 
 * @author Affehund
 *
 */
@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Bus.MOD)
public class VoidTotemConfig {
	public static class VoidCommonConfig {
		public final BooleanValue ADD_END_CITY_TREASURE;
		public final BooleanValue ALLOW_TOTEM_OF_UNDYING;
		public final ConfigValue<ArrayList<String>> BLACKLISTED_DIMENSIONS;
		public final BooleanValue NEEDS_TOTEM;
		public final BooleanValue SHOW_TOTEM_TOOLTIP;
		public final IntValue TELEPORT_HEIGHT;
		public final BooleanValue USE_TOTEM_FROM_INVENTORY;

		public VoidCommonConfig(ForgeConfigSpec.Builder builder) {
			builder.comment("Void Totem Common Config").push("general");
			ADD_END_CITY_TREASURE = builder
					.comment("This sets whether the void totem will be added to the end city treasure.")
					.define("add_end_city_treasure", true);
			ALLOW_TOTEM_OF_UNDYING = builder
					.comment("This sets whether the totem of undying will prevent death when falling into the void.")
					.define("allow_totem_of_undying", false);
			BLACKLISTED_DIMENSIONS = builder.comment(
					"This adds dimensions to a blacklist where you die if you fall into the void. Example: \"minecraft:overworld\".")
					.define("blacklisted_dimensions", new ArrayList<String>());
			NEEDS_TOTEM = builder
					.comment("This sets whether you need a totem to prevent death when falling into the void.")
					.define("needs_totem", true);
			SHOW_TOTEM_TOOLTIP = builder.comment("This sets whether a tooltip is show on the totem.")
					.define("show_totem_tooltip", true);
			TELEPORT_HEIGHT = builder
					.comment("This sets the height you will be teleported when you can't be placed on a block.")
					.defineInRange("teleport_height", 320, 256, 2048);
			USE_TOTEM_FROM_INVENTORY = builder.comment(
					"This sets whether the totem prevents you from dying in the void if there is a totem anywhere in your inventory. If false the totem has to been in the main-/offhand or in the charm slot (curios api has to be installed).")
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

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading event) {
		VoidTotem.LOGGER.info("Loaded {} config file from {}", event.getConfig().getFileName(), ModConstants.MOD_ID);
	}
}
