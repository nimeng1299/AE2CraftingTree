package com.neuvillette.ae2ct;


import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CONFIG;

    public static ForgeConfigSpec.BooleanValue USE_COMPACT_TREE;
    public static ForgeConfigSpec.BooleanValue SCREENSHOT_SHOW_COUNT;

    static {
        USE_COMPACT_TREE = BUILDER.comment("true: compact, false: loose").define("use_compact_tree", false);
        SCREENSHOT_SHOW_COUNT = BUILDER.comment("show count in screenshot").define("screenshot_show_count", false);

        CONFIG = BUILDER.build();
    }

    public static void save(){
        CONFIG.save();
    }
}
