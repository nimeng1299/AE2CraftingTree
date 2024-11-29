package com.neuvillette.ae2ct.api;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.neuvillette.ae2ct.AE2ct;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static net.minecraft.client.Screenshot.takeScreenshot;

public class ScreenshotHelper {
    private final static int scale = 2;

    public static void Screenshot(CraftingTreeHelper.NodeInfo nodeInfo, Player player) {
        try {
            int scale = 2;
            Minecraft minecraft = Minecraft.getInstance();

            Map<AEKey, Point>map = new HashMap<>();

            BufferedImage image = new BufferedImage(nodeInfo.max_x() * 110, nodeInfo.max_y() * 110, 6);
            var graphics = image.createGraphics();
            graphics.setColor(Color.BLACK);
            graphics.setStroke(new BasicStroke(4));
            graphics.setBackground(Color.GRAY);

            BufferedImage stackImage = init(nodeInfo, map);

            draw(graphics, stackImage, nodeInfo.node(), map);


            graphics.dispose();

            safeImage(minecraft.gameDirectory, "CraftingTree_" + Util.getFilenameFormattedDateTime() + ".png", image, player::sendSystemMessage);
            //_grab(minecraft.gameDirectory, "CraftingTree_" + Util.getFilenameFormattedDateTime() + ".png", target, player::sendSystemMessage);

        }
        catch (Exception e)
        {
            player.sendSystemMessage(Component.translatable("ae2ct.screenshot.exception", e.toString()));
        }

    }

    private static BufferedImage init(CraftingTreeHelper.NodeInfo nodeInfo, Map<AEKey, Point> map) throws IOException {
        Minecraft minecraft = Minecraft.getInstance();

        Set<AEKey> keys = new HashSet<>();
        initNode(nodeInfo.node(), keys);
        int size = keys.size();
        int len = ((int) Math.sqrt(size)) + 1;

        int simpleLen = 22;

        int width = len * simpleLen * scale * 2;
        int height = len * simpleLen * scale * 2;

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
        view.scale(4f / width, -4f / height, -1f / 1000f);
        view.translate(0.0f, 0.0f, 10.0f);
        RenderSystem.applyModelViewMatrix();
        Matrix4f backupProj = RenderSystem.getProjectionMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f().identity(), VertexSorting.ORTHOGRAPHIC_Z);

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        GuiGraphics guiGraphics = new GuiGraphics(minecraft, bufferSource);
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);

        guiGraphics.fill(0, 0, width / scale, height / scale, 0xffcbccd4);

        int x = 0;
        int y = 0;
        for(AEKey key : keys){
            Point pos = new Point(x, y);
            map.put(key, pos);

            guiGraphics.blit(ResourceLocation.tryBuild(AE2ct.MODID, "icon.png"), x * simpleLen, y * simpleLen, 0, 0, 22, 22);
            AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, x * simpleLen + 3, y * simpleLen + 3, key);
            x++;
            if (x >= len){
                x = 0;
                y++;
            }
        }


        guiGraphics.flush();

        RenderSystem.setProjectionMatrix(backupProj, VertexSorting.ORTHOGRAPHIC_Z);
        view.popPose();
        RenderSystem.applyModelViewMatrix();
        target.unbindWrite();
        NativeImage nativeimage = takeScreenshot(target);
        var img =  ImageIO.read(new ByteArrayInputStream(nativeimage.asByteArray()));
        target.destroyBuffers();
        return img;
    }

    private static void initNode(CraftingTreeHelper.Node node, Set<AEKey> set){
        set.add(node.stack().what());
        if(node.subNodes() != null){
            for (var subNode : node.subNodes()){
                initNode(subNode, set);
            }
        }
    }

    private static void draw(Graphics2D graphics, BufferedImage stackImage, CraftingTreeHelper.Node node, Map<AEKey, Point> map){
        int spacing = 110; // 88 + 22
        int output = 10;
        int stackLength = 44;
        int x = node.position().x * spacing + output;
        int y = node.position().y * spacing + output;
        if(node.subNodes() != null) graphics.drawLine(x + stackLength, y + stackLength, x + stackLength, y + stackLength + spacing / 2);

        Point pos = map.get(node.stack().what());
        BufferedImage subImage = stackImage.getSubimage(pos.x * 88, pos.y * 88, 88, 88);
        graphics.drawImage(subImage, x, y, null);

        if(node.subNodes() == null) return;
        Point last = new Point(0, 0);
        for(var child : node.subNodes()){
            var p = child.position();
            var pX = p.x * spacing + output;
            var pY = p.y * spacing + output;
            graphics.drawLine(pX + stackLength, y + stackLength + spacing / 2, pX + stackLength, pY + stackLength);
            draw(graphics, stackImage, child, map);
            if(last.x < p.x){
                last = p;
            }
        }
        graphics.drawLine(x + stackLength, y + stackLength + spacing / 2, last.x * spacing + output + stackLength, y + stackLength + spacing / 2);

    }

    private static void safeImage(File file, @Nullable String p_92307_, BufferedImage image, Consumer<Component> p_92311_) throws IOException {
        File file1 = new File(file, "screenshots");
        file1.mkdir();
        File file2;
        if (p_92307_ == null) {
            file2 = getFile(file1);
        } else {
            file2 = new File(file1, p_92307_);
        }
        final File target = file2.getCanonicalFile();
        Util.ioPool().execute(() -> {
            try {
                boolean success = ImageIO.write(image, "png", target);
                if (!success) {
                    throw new IOException("Failed to write buffered image to file: " + target.getAbsolutePath());
                }
                Component component = Component.literal(file2.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle((p_168608_) -> {
                    return p_168608_.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, target.getAbsolutePath()));
                });
                p_92311_.accept(Component.translatable("screenshot.success", component));
            } catch (Exception exception) {
                p_92311_.accept(Component.translatable("screenshot.failure", exception.getMessage()));
            }

        });
    }



    private static File getFile(File p_92288_) {
        String s = Util.getFilenameFormattedDateTime();
        int i = 1;

        while(true) {
            File file1 = new File(p_92288_, s + (i == 1 ? "" : "_" + i) + ".png");
            if (!file1.exists()) {
                return file1;
            }

            ++i;
        }
    }
}
