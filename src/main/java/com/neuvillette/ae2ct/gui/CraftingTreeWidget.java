package com.neuvillette.ae2ct.gui;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.crafting.CraftingCalculation;
import appeng.menu.me.crafting.CraftingPlanSummary;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;

public class CraftingTreeWidget implements ICompositeWidget {
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);

    @Override
    public void setPosition(Point position) {
        bounds = new Rect2i(position.getX(), position.getY(), bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public void setSize(int width, int height) {
        bounds = new Rect2i(bounds.getX(), bounds.getY(), width, height);
    }

    @Override
    public Rect2i getBounds() {
        return bounds;
    }

    public void draw(int mouseX, int mouseY){

    }
}
