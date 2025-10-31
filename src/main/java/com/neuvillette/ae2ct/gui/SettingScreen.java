package com.neuvillette.ae2ct.gui;

import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.TabButton;
import appeng.menu.me.crafting.CraftConfirmMenu;
import com.neuvillette.ae2ct.Config;
import com.neuvillette.ae2ct.api.ToolTipText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;

public class SettingScreen extends AESubScreen<CraftConfirmMenu, CraftingTreeScreen> {
    private final AECheckbox useCompactTreeCheckbox;
    private final AECheckbox screenShotShowCountCheckbox;
    private final AECheckbox showMissingOnlyByDefaultCheckbox;

    public SettingScreen(CraftingTreeScreen parent) {
        super(parent, "/screens/crafting_tree_setting.json");
        addBackButton();

        this.useCompactTreeCheckbox = this.widgets.addCheckbox("useCompactTreeCheckbox", ToolTipText.UseCompactTreeCheckbox.text(), this::save);
        this.screenShotShowCountCheckbox = this.widgets.addCheckbox("screenShotShowCountCheckbox", ToolTipText.ScreenShotShowCountCheckbox.text(), this::save);
        this.showMissingOnlyByDefaultCheckbox = this.widgets.addCheckbox("showMissingOnlyByDefaultCheckbox", ToolTipText.ShowMissingOnlyByDefault.text(), this::save);
        updateState();
    }

    private void addBackButton() {
        var icon = Icon.CRAFT_HAMMER;
        var label = ToolTipText.ShowTree.text();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        TabButton button = new TabButton(icon, label, btn -> returnToParent());
        widgets.add("back", button);
    }

    private void updateState(){
        this.useCompactTreeCheckbox.setSelected(Config.USE_COMPACT_TREE.get());
        this.screenShotShowCountCheckbox.setSelected(Config.SCREENSHOT_SHOW_COUNT.get());
        this.showMissingOnlyByDefaultCheckbox.setSelected(Config.SHOW_MISSING_ONLY_BY_DEFAULT.get());
    }

    private void save(){
        Config.USE_COMPACT_TREE.set(this.useCompactTreeCheckbox.isSelected());
        Config.SCREENSHOT_SHOW_COUNT.set(this.screenShotShowCountCheckbox.isSelected());
        Config.SHOW_MISSING_ONLY_BY_DEFAULT.set(this.showMissingOnlyByDefaultCheckbox.isSelected());
        Config.save();
        updateState();
    }
}
