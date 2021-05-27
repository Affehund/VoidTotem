package com.affehund.voidtotem.core;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.affehund.voidtotem.VoidTotemFabric;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import net.fabricmc.loader.api.FabricLoader;

/**
 * @author Affehund
 *
 */
public class VoidTotemConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final File FILE = new File(
			FabricLoader.getInstance().getConfigDir() + ModConstants.COMMON_CONFIG_NAME);

	public Boolean ADD_END_CITY_TREASURE;
	public Boolean ALLOW_TOTEM_OF_UNDYING;
	public ArrayList<String> BLACKLISTED_DIMENSIONS;
	public Boolean NEEDS_TOTEM;
	public Boolean SHOW_TOTEM_TOOLTIP;
	public int TELEPORT_HEIGHT;
	public Boolean USE_TOTEM_FROM_INVENTORY;

	public VoidTotemConfig() {
		this.ADD_END_CITY_TREASURE = true;
		this.ALLOW_TOTEM_OF_UNDYING = false;
		this.BLACKLISTED_DIMENSIONS = new ArrayList<String>();
		this.NEEDS_TOTEM = true;
		this.SHOW_TOTEM_TOOLTIP = true;
		this.TELEPORT_HEIGHT = 320;
		this.USE_TOTEM_FROM_INVENTORY = false;
	}

	public VoidTotemConfig(VoidTotemConfig config) {
		this.ALLOW_TOTEM_OF_UNDYING = config.ALLOW_TOTEM_OF_UNDYING;
		this.BLACKLISTED_DIMENSIONS = config.BLACKLISTED_DIMENSIONS;
		this.NEEDS_TOTEM = config.NEEDS_TOTEM;
		this.SHOW_TOTEM_TOOLTIP = config.SHOW_TOTEM_TOOLTIP;
		this.TELEPORT_HEIGHT = config.TELEPORT_HEIGHT;
		this.USE_TOTEM_FROM_INVENTORY = config.USE_TOTEM_FROM_INVENTORY;
	}

	public static VoidTotemConfig setup() {
		if (!FILE.exists()) {
			VoidTotemConfig config = new VoidTotemConfig();
			config.create();
			return config;
		}
		try {
			FileReader fileReader = new FileReader(FILE);
			VoidTotemConfig config = GSON.fromJson(fileReader, VoidTotemConfig.class);
			VoidTotemFabric.LOGGER.debug("Reading config {}", FILE.getName());
			return config != null ? config : new VoidTotemConfig();
		} catch (IOException | JsonIOException | JsonSyntaxException e) {
			VoidTotemFabric.LOGGER.error(e.getMessage(), e);
			return new VoidTotemConfig();
		}
	}

	public void create() {
		try (FileWriter fileWriter = new FileWriter(FILE)) {
			fileWriter.write(GSON.toJson(this));
			VoidTotemFabric.LOGGER.debug("Created new config {}", FILE.getName());
		} catch (IOException e) {
			VoidTotemFabric.LOGGER.error(e.getMessage(), e);
		}
	}
}
