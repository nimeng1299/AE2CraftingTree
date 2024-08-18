package com.neuvillette.ae2ct.gui;


import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import appeng.core.localization.LocalizationEnum;
import com.neuvillette.ae2ct.api.ToolTipText;
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
        ToolTipText displayName = ToolTipText.ShowTree;
        String name = displayName.text().getString();
        return Component.literal(name);
    }


}
