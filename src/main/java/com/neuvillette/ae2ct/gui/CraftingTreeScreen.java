package com.neuvillette.ae2ct.gui;

import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.crafting.CraftConfirmScreen;

import appeng.client.gui.widgets.TabButton;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import com.neuvillette.ae2ct.api.ICraftingPlanSummary;
import com.neuvillette.ae2ct.api.ScreenshotHelper;
import com.neuvillette.ae2ct.api.ToolTipText;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;
import java.util.List;

public class CraftingTreeScreen extends AESubScreen<CraftConfirmMenu, CraftConfirmScreen> {
    private final CraftingTreeWidget craftingTreeWidget;
    private final CraftConfirmScreen parent;

     public CraftingTreeScreen(CraftConfirmScreen parent) {
         super(parent, "/screens/crafting_tree.json");
         this.parent = parent;
         craftingTreeWidget = new CraftingTreeWidget(this, ((ICraftingPlanSummary)parent.getMenu().getPlan()).getJob(), parent.getMenu().getPlan().getEntries());
         addBackButton();
         this.addToLeftToolbar(new ChangeButton(craftingTreeWidget::screenShot, Icon.STORAGE_FILTER_EXTRACTABLE_ONLY, ToolTipText.Screenshot));

    }

    private void addBackButton() {
        var label = menu.getHost().getMainMenuIcon().getHoverName();
        TabButton button = new TabButton(Icon.BACK, label, btn -> returnToParent());
        widgets.add("back", button);
    }


    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);
        craftingTreeWidget.draw(guiGraphics, offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        craftingTreeWidget.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
         craftingTreeWidget.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
         return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
    }
    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        craftingTreeWidget.mouseClicked(xCoord, yCoord, btn);
        return super.mouseClicked(xCoord, yCoord, btn);
    }
}
