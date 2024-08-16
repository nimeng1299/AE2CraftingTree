package com.neuvillette.ae2ct.gui;


import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import appeng.core.localization.LocalizationEnum;
import net.minecraft.network.chat.Component;

public class ChangeButton extends IconButton {
    public ChangeButton(Runnable onPress) {
        super(btn -> onPress.run());
        setMessage(buildMessage());
    }

    @Override
    protected Icon getIcon() {
        return Icon.CRAFT_HAMMER;
    }

    private Component buildMessage() {
        ButtonToolTip displayName = ButtonToolTip.Show;
        String name = displayName.text().getString();
        return Component.literal(name);
    }

    public enum ButtonToolTip implements LocalizationEnum {
        Show("Show Crafting Tree"),
        ;
        private final String englishText;

        ButtonToolTip(String englishText) {
            this.englishText = englishText;
        }

        @Override
        public String getTranslationKey() {
            return "gui.tooltips.ae2ct." + name();
        }

        @Override
        public String getEnglishText() {
            return englishText;
        }

    }
}
