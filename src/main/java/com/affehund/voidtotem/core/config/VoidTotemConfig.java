package com.affehund.voidtotem.core.config;

import com.affehund.voidtotem.core.ModConstants;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;

@Config(name = ModConstants.MOD_ID)
public class VoidTotemConfig implements ConfigData {

    @Comment("This sets whether the void totem will be added to the end city treasure.")
    public Boolean ADD_END_CITY_TREASURE = true;

    @Comment("This adds dimensions to a blacklist where you die if you fall into the void. Example: \"minecraft:overworld\"")
    public ArrayList<String> BLACKLISTED_DIMENSIONS = new ArrayList<>();

    @Comment("This sets whether you need a totem to prevent death when falling into the void.")
    public Boolean NEEDS_TOTEM = true;

    @Comment("This sets whether a tooltip is shown on the totem.")
    public Boolean SHOW_TOTEM_TOOLTIP = false;

    @Comment("This sets the height offset you will be teleported when you can't be placed on a block.")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 1024)
    public int TELEPORT_HEIGHT_OFFSET = 64;

    @Comment("This sets whether you can use a totem from anywhere in your inventory.")
    public Boolean USE_TOTEM_FROM_INVENTORY = false;
}
