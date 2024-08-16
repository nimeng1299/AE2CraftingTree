package com.neuvillette.ae2ct.gui;

import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.crafting.CraftConfirmScreen;

import appeng.client.gui.widgets.TabButton;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import com.neuvillette.ae2ct.api.ICraftingPlanSummary;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class CraftingTreeScreen extends AESubScreen<CraftConfirmMenu, CraftConfirmScreen> {
    private final CraftingTreeWidget craftingTreeWidget;
    private final CraftConfirmScreen parent;

     public CraftingTreeScreen(CraftConfirmScreen parent) {
         super(parent, "/screens/crafting_tree.json");
         craftingTreeWidget = new CraftingTreeWidget();
         this.parent = parent;
         var res = parent.getMenu().getPlan();
         var a = ((ICraftingPlanSummary)res).getJob();
         List<CraftingPlanSummaryEntry> craftingPlanSummary = parent.getMenu().getPlan().getEntries();
         addBackButton();
         addCraftingTreeWidget();

    }

    private void addBackButton() {
        var label = menu.getHost().getMainMenuIcon().getHoverName();
        TabButton button = new TabButton(Icon.BACK, label, btn -> returnToParent());
        widgets.add("back", button);
    }

    private void addCraftingTreeWidget() {
        widgets.add("tree", craftingTreeWidget);
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);
        craftingTreeWidget.draw(mouseX, mouseY);
    }





}
