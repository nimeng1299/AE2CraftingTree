package com.neuvillette.ae2ct;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder  BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec CONFIG;

    public static ModConfigSpec.BooleanValue USE_COMPACT_TREE;
    public static ModConfigSpec.BooleanValue SCREENSHOT_SHOW_COUNT;

    static {
        USE_COMPACT_TREE = BUILDER.comment("true: compact, false: loose").define("use_compact_tree", false);
        SCREENSHOT_SHOW_COUNT = BUILDER.comment("show count in screenshot").define("screenshot_show_count", false);

        CONFIG = BUILDER.build();
    }

    public static void save(){
        CONFIG.save();
    }
}
