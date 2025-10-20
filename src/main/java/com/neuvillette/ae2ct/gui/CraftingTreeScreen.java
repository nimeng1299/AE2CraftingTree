package com.neuvillette.ae2ct.gui;

import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.crafting.CraftConfirmScreen;

import appeng.client.gui.widgets.TabButton;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;

import com.mojang.blaze3d.platform.InputConstants;
import com.neuvillette.ae2ct.Config;
import com.neuvillette.ae2ct.api.ICraftingPlanSummary;
import com.neuvillette.ae2ct.api.ToolTipText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import java.util.List;

public class CraftingTreeScreen extends AESubScreen<CraftConfirmMenu, CraftConfirmScreen> {
    private  CraftingTreeWidget craftingTreeWidget;

     public CraftingTreeScreen(CraftConfirmScreen parent) {
         super(parent, "/screens/crafting_tree.json");
         craftingTreeWidget = new CraftingTreeWidget(this, ((ICraftingPlanSummary)parent.getMenu().getPlan()).getJob(), parent.getMenu().getPlan().getEntries(), Config.SHOW_MISSING_ONLY_BY_DEFAULT.get());
         addBackButton();
         this.addToLeftToolbar(new ChangeButton(this::changeSetting, Icon.WRENCH, ToolTipText.Setting));
         this.addToLeftToolbar(new ChangeButton(craftingTreeWidget::screenShot, Icon.STORAGE_FILTER_EXTRACTABLE_ONLY, ToolTipText.Screenshot));
         this.addToLeftToolbar(new ChangeButton(this::showMissingOnly, Icon.INVALID, ToolTipText.ShowMissingOnly));
    }

    private void addBackButton() {
         var icon = menu.getHost().getMainMenuIcon();
         var label = icon.getHoverName();
         ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
         TabButton button = new TabButton(icon, label, btn -> returnToParent());
         widgets.add("back", button);
    }

    private void showMissingOnly() {
        if(craftingTreeWidget == null) return;
        craftingTreeWidget.isMissingOnly = !craftingTreeWidget.isMissingOnly;
        craftingTreeWidget = new CraftingTreeWidget(this, ((ICraftingPlanSummary) menu.getPlan()).getJob(), menu.getPlan().getEntries(), craftingTreeWidget.isMissingOnly);
    }

    private void changeSetting(){
            switchToScreen(new SettingScreen(this));
    }


    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);
        craftingTreeWidget.draw(guiGraphics, offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double wheelDelta) {
        craftingTreeWidget.mouseScrolled(mouseX, mouseY, wheelDelta, wheelDelta);
        return super.mouseScrolled(mouseX, mouseY, wheelDelta);
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        craftingTreeWidget.keyPressed(keyCode, scanCode, modifiers);
        return true;
    }
}
