package com.neuvillette.ae2ct.gui;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AmountFormat;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.neuvillette.ae2ct.AE2ct;
import com.neuvillette.ae2ct.api.CraftingTreeHelper;
import com.neuvillette.ae2ct.api.RecipeHelper;
import com.neuvillette.ae2ct.api.ToolTipText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class CraftingTreeWidget {
    private RecipeHelper data;
    protected final AEBaseScreen<?> screen;
    private CompletableFuture<CraftingTreeHelper.Node> future = null;
    private CraftingTreeHelper helper;
    private int outputX = 20;
    private int outputY = 30;
    private int spacingX = 30;
    private int spacingY = 30;
    private int stackLength = 8;
    private float scroll = 1.0f;
    public CraftingTreeWidget(AEBaseScreen<?> screen, RecipeHelper data) {
        this.screen = screen;
        this.data = data;
        this.helper = new CraftingTreeHelper(data);
        this.future = CompletableFuture.supplyAsync(() -> helper.buildNode());
    }


    public void draw(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY){
        var color = FastColor.ARGB32.color(100, 114, 114, 114);

        var board =  new Rect2i(screen.getGuiLeft() + 10 , screen.getGuiTop() + 20, 180, 180);
        guiGraphics.enableScissor(board.getX(), board.getY(), board.getX() + board.getWidth(), board.getY() + board.getHeight());
        //guiGraphics.fill(0, 0, 30000, 30000, color);

        var output = data.output;
        //AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, board.getX() + 10, board.getY() - 40 , output.what());
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        try {
            if (future.isDone()) {
                var node = future.get();
                poseStack.scale(scroll, scroll, scroll);
                drawNode(guiGraphics, node);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        poseStack.popPose();
        guiGraphics.disableScissor();
        Point p = getMousePoint(guiGraphics, mouseX, mouseY);
        if(this.helper.getNodesMap().containsKey(p)){
            var node = this.helper.getNodesMap().get(p);
            var stack = node.stack();
            var x = mouseX - screen.getGuiLeft() + 10;
            var y = mouseY - screen.getGuiTop() + 10;
            var lines =AEKeyRendering.getTooltip(stack.what());
            if(stack.what() == this.data.output.what()){
                lines.add(ToolTipText.OutputAmount.text(stack.what().formatAmount(node.amount(), AmountFormat.FULL)));
            }else if(node.subNodes() == null || node.subNodes().isEmpty()){
                lines.add(ToolTipText.InputAmount.text(stack.what().formatAmount(node.amount(), AmountFormat.FULL)));
            }else{
                lines.add(ToolTipText.MiddenAmount.text(stack.what().formatAmount(node.amount(), AmountFormat.FULL)));
            }
            screen.drawTooltipWithHeader(guiGraphics, mouseX - screen.getGuiLeft(), mouseY - screen.getGuiTop(), lines);
        }
    }

    private void drawNode(GuiGraphics guiGraphics, CraftingTreeHelper.Node node){
        var stack = node.stack();
        var color = FastColor.ARGB32.color(255, 0, 0, 0);
        int x = node.position().x * spacingX + outputX;
        int y = node.position().y * spacingY + outputY;

        if(node.subNodes() != null) guiGraphics.vLine(x + stackLength, y + stackLength, y + stackLength + spacingY / 2, color);
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(AE2ct.MODID, "icon.png"), x - 3 , y - 3, 0, 0, 22, 22);
        AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, x, y, stack.what());
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        var font = Minecraft.getInstance().font;
        var fontX = font.width(getDrawAmount(node));
        var fontY = font.lineHeight;

        float scale = 0.4f;
        poseStack.scale(scale, scale, scale);

        guiGraphics.drawString(font, getDrawAmount(node), (x + 18 - fontX * scale) / scale, (y + 18 - fontY * scale) / scale, color, false);
        poseStack.popPose();
        if(node.subNodes() == null) return;
        Point last = new Point(0, 0);
        for(var child : node.subNodes()){
            var p = child.position();
            var pX = p.x * spacingX + outputX;
            var pY = p.y * spacingY + outputY;
            guiGraphics.vLine(pX + stackLength, y + stackLength + spacingY / 2, pY + stackLength, color);
            drawNode(guiGraphics, child);
            if(last.x < p.x){
                last = p;
            }
        }
        guiGraphics.hLine(x + stackLength, last.x * spacingX + outputX + stackLength, y + stackLength + spacingY / 2, color);
    }

    private String getDrawAmount(CraftingTreeHelper.Node node){
        var amount = node.amount();
        if(node.stack().what() instanceof AEFluidKey){
            if (amount >= 1_000) {
                return formatNumber(amount / 1_000.0) + "B";
            } else {
                return String.valueOf(amount);
            }
        }else{
            if (amount >= 1_000_000_000) {
                return formatNumber(amount / 1_000_000_000.0) + "G";
            } else if (amount >= 1_000_000) {
                return formatNumber(amount / 1_000_000.0) + "M";
            } else if (amount >= 1_000) {
                return formatNumber(amount / 1_000.0) + "k";
            } else {
                return String.valueOf(amount);
            }
        }
    }
    private static String formatNumber(double number) {
        if (number == (long) number) {
            return String.format("%d", (long) number);
        } else {
            return String.format("%.1f", number);
        }
    }


    private Rect2i getArea(){
        return new Rect2i(10, 20, 200, 190);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if(isMouseOutScreen(mouseX, mouseY)) return true;
        scroll += deltaY * 0.1;
        if (scroll <= 0f) scroll = 0.1f;
        if (scroll >= 10f) scroll = 10f;
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        if(isMouseOutScreen(mouseX, mouseY)) return true;
        if(mouseButton == 1){
            outputX += (int) dragX;
            outputY += (int) dragY;
        }
        return true;
    }

    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        if(btn == 2){
            scroll = 1.0f;
            outputX = 20;
            outputY = 30;
        }
        return true;
    }

    private boolean isMouseOutScreen(double mouseX, double mouseY){
        if(mouseX - screen.getGuiLeft() < getArea().getX() || mouseX - screen.getGuiLeft() > getArea().getX() + getArea().getWidth()) return true;
        if(mouseY - screen.getGuiTop() < getArea().getY() || mouseY - screen.getGuiTop() > getArea().getY() + getArea().getHeight()) return true;
        return false;
    }

    private Point getMousePoint(GuiGraphics guiGraphics, double mouseX, double mouseY){
        if(isMouseOutScreen(mouseX, mouseY)) return new Point(-1, -1);
        int x = (int) (mouseX - screen.getGuiLeft() - outputX * scroll);
        int y = (int) (mouseY - screen.getGuiTop() - outputY * scroll);
        int sizeX = (int) ( spacingX * scroll);
        int sizeY = (int) (spacingY * scroll);
        int i = x / sizeX;
        int j = y / sizeY;
        int left = (int) (i * spacingX * scroll);
        int top = (int) (j * spacingY * scroll);
        int right = (int) ((i * spacingX + stackLength * 2)  * scroll);
        int bottom = (int) ((j * spacingY + stackLength * 2) * scroll);
        if(x > left && x < right && y > top && y < bottom){
            return new Point(i, j);
        } else return new Point(-1, -1);
    }
}
