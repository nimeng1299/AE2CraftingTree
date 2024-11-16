package com.neuvillette.ae2ct.gui;


import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import appeng.core.localization.LocalizationEnum;
import com.neuvillette.ae2ct.api.ToolTipText;
import net.minecraft.network.chat.Component;

public class ChangeButton extends IconButton {
    Icon icon;
    public ChangeButton(Runnable onPress, Icon icon, ToolTipText displayName) {
        super(btn -> onPress.run());
        this.icon = icon;
        setMessage(buildMessage(displayName));
    }

    @Override
    protected Icon getIcon() {
        return icon;
    }

    private Component buildMessage(ToolTipText displayName) {
        String name = displayName.text().getString();
        return Component.literal(name);
    }


}
