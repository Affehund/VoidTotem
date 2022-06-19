package com.affehund.voidtotem.core;

import blue.endless.jankson.Comment;
import com.affehund.voidtotem.ModConstants;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

@Config(name = ModConstants.MOD_ID)
public class VoidTotemConfig implements ConfigData {

    @Comment("Whether the void totem should be added to the end city treasure.")
    public Boolean ADD_END_CITY_TREASURE = true;

    @Comment("Dimensions in this blacklist will prevent the functionality of the void totem. Example: \"minecraft:overworld\"")
    public List<String> BLACKLISTED_DIMENSIONS = new ArrayList<>();

    @Comment("Whether the void totem should be displayed on the chest when worn in the trinkets charm slot (trinkets mod must be installed).")
    public Boolean DISPLAY_TOTEM_ON_CHEST = true;

    @Comment("Whether you get the regeneration and absorption effect on the void totem execution. This also removes all previous effects.")
    public Boolean GIVE_TOTEM_EFFECTS = false;

    @Comment("Whether the blacklist is inverted, meaning the void totem only works in whitelisted dimensions.")
    public Boolean IS_INVERTED_BLACKLIST = false;

    @Comment("Whether you need a totem to prevent death when falling into the void.")
    public Boolean NEEDS_TOTEM = true;

    @Comment("Whether a tooltip is shown on the totem.")
    public Boolean SHOW_TOTEM_TOOLTIP = false;

    @Comment("The height offset from the world height you will be teleported if you can't be placed on a block.")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 1024)
    public int TELEPORT_HEIGHT_OFFSET = 64;

    @Comment("Whether you can use the void totem from anywhere in your inventory.")
    public Boolean USE_TOTEM_FROM_INVENTORY = false;
}
