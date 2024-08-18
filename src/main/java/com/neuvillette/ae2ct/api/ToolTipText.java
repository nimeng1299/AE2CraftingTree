package com.neuvillette.ae2ct.api;

import appeng.core.localization.LocalizationEnum;

import net.minecraft.network.chat.Component;

public enum ToolTipText implements LocalizationEnum {
    ShowTree("Show Crafting Tree"),
    OutputAmount("Output: %s"),
    InputAmount("need: %s"),
    MiddenAmount("use: %s"),
    ;
    private final String englishText;

    private final Component text;

    ToolTipText(String englishText) {
        this.englishText = englishText;
        this.text = Component.translatable("gui.ae2ct." + name());
    }

    @Override
    public String getTranslationKey() {
        return "gui.ae2ct." + name();
    }

    @Override
    public String getEnglishText() {
        return englishText;
    }

    public String getLocal() {
        return text.getString();
    }
}
