package com.neuvillette.ae2ct.api;

import appeng.api.client.AEKeyRendering;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.neuvillette.ae2ct.AE2ct;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.awt.*;

public class ScreenshotHelper {

    public static void Screenshot(CraftingTreeHelper.NodeInfo nodeInfo, Player player) {
        try {
            float scale = 2.0f;
            Minecraft minecraft = Minecraft.getInstance();

            int width = (int) ((nodeInfo.max_x() * 18 + (nodeInfo.max_x() - 1) * 12 + 15 * 2) * scale * 3);
            int height = (int) ((nodeInfo.max_y() * 18 + (nodeInfo.max_y() - 1) * 12 + 15 * 2) * scale * 3);

            if(width > 32767)
            {
                scale = 32767.0f / (width + 0.1f) * scale;
                width = (int) ((nodeInfo.max_x() * 18 + (nodeInfo.max_x() - 1) * 12 + 15 * 2) * scale * 3);
                height = (int) ((nodeInfo.max_y() * 18 + (nodeInfo.max_y() - 1) * 12 + 15 * 2) * scale * 3);
            }

            if(height > 32767)
            {
                scale = (32767.0f / (height + 0.1f)) * scale;
                width = (int) ((nodeInfo.max_x() * 18 + (nodeInfo.max_x() - 1) * 12 + 15 * 2) * scale * 3);
                height = (int) ((nodeInfo.max_y() * 18 + (nodeInfo.max_y() - 1) * 12 + 15 * 2) * scale * 3);
            }

            RenderTarget target = new RenderTarget(true) {

            };
            target.createBuffers(width, height, true);

            target.setClearColor(203, 204, 212, 255);
            target.clear(Minecraft.ON_OSX);
            target.bindWrite(true);

            PoseStack view = RenderSystem.getModelViewStack();
            view.pushPose();
            view.setIdentity();
            view.translate(-1.0f, 1.0f, 0.0f);
            view.scale(6f / width, -6f / height, -1f / 1000f);
            view.translate(0.0f, 0.0f, 10.0f);
            RenderSystem.applyModelViewMatrix();

            Matrix4f backupProj = RenderSystem.getProjectionMatrix();
            RenderSystem.setProjectionMatrix(new Matrix4f().identity(), VertexSorting.ORTHOGRAPHIC_Z);


            MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
            GuiGraphics guiGraphics = new GuiGraphics(minecraft, bufferSource);

            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();


            guiGraphics.fill(0, 0, width, height, 0xffcbccd4);

            poseStack.scale(scale, scale, scale);

            drawNode(guiGraphics, nodeInfo.node());


            guiGraphics.flush();
            RenderSystem.setProjectionMatrix(backupProj, VertexSorting.ORTHOGRAPHIC_Z);
            view.popPose();
            RenderSystem.applyModelViewMatrix();
            target.unbindWrite();
            target.bindRead();
            Screenshot.grab(minecraft.gameDirectory, "CraftingTree_" + Util.getFilenameFormattedDateTime() + ".png", target, player::sendSystemMessage);
            target.unbindRead();

        }
        catch (Exception e)
        {
            player.sendSystemMessage(Component.translatable("ae2ct.screenshot.exception", e.toString()));
        }

    }

    private static void drawNode(GuiGraphics guiGraphics, CraftingTreeHelper.Node node) {
        int spacingX = 30;
        int spacingY = 30;
        int outputX = 15;
        int outputY = 15;
        int stackLength = 8;

        var stack = node.stack();
        var color = FastColor.ARGB32.color(255, 0, 0, 0);
        int x = node.position().x * spacingX + outputX;
        int y = node.position().y * spacingY + outputY;
        if(node.subNodes() != null) guiGraphics.vLine(x + stackLength, y + stackLength, y + stackLength + spacingY / 2, color);
        guiGraphics.blit(ResourceLocation.tryBuild(AE2ct.MODID, "icon.png"), x - 3, y - 3, 0, 0, 22, 22);

        AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, x, y, stack.what());

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
}
