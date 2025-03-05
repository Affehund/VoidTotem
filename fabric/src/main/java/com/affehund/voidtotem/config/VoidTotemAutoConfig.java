package com.affehund.voidtotem.config;

import com.affehund.voidtotem.ModConstants;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.List;

@Config(name = ModConstants.MOD_ID)
public class VoidTotemAutoConfig implements ConfigData {
    
    @Comment("Dimensions in this blocklist will prevent the functionality of the void totem. Example: \"minecraft:overworld\"")
    public List<String> BLOCKLISTED_DIMENSIONS = new ArrayList<>();

    @Comment("Whether the blocklist is inverted, meaning the void totem only works in allowlisted dimensions.")
    public Boolean IS_INVERTED_BLOCKLIST = false;

    @Comment("The height offset from the world height you will be teleported if you can't be placed on a block.")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 1024)
    public int TELEPORT_HEIGHT_OFFSET = 64;

    @Comment("Whether you can use the void totem from anywhere in your inventory.")
    public Boolean USE_TOTEM_FROM_INVENTORY = false;
}
