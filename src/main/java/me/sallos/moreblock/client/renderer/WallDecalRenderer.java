package me.sallos.moreblock.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.wall.WallDecalSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = Moreblock.MODID, value = Dist.CLIENT)
public final class WallDecalRenderer {
    private static final float DECAL_OFFSET = 0.002f;

    private WallDecalRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        render(event.getPoseStack(), event.getCamera());
    }

    private static void render(PoseStack poseStack, Camera camera) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        double cameraX = camera.getPosition().x;
        double cameraY = camera.getPosition().y;
        double cameraZ = camera.getPosition().z;

        poseStack.pushPose();
        poseStack.translate(-cameraX, -cameraY, -cameraZ);
        Matrix4f matrix = poseStack.last().pose();
        for (WallDecalSystem.DecalPlacement placement : WallDecalSystem.getPlacements(level.dimension())) {
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(placement.texture()));
            renderFace(level, matrix, consumer, placement.pos(), placement.face());
        }
        poseStack.popPose();
        bufferSource.endBatch();
    }

    private static void renderFace(Level level, Matrix4f matrix, VertexConsumer consumer, BlockPos pos, Direction face) {
        int packedLight = LevelRenderer.getLightColor(level, pos.relative(face));
        float minX = pos.getX();
        float minY = pos.getY();
        float minZ = pos.getZ();
        float maxX = minX + 1.0f;
        float maxY = minY + 1.0f;
        float maxZ = minZ + 1.0f;

        switch (face) {
            case NORTH -> quad(matrix, consumer, maxX, minY, minZ - DECAL_OFFSET, minX, minY, minZ - DECAL_OFFSET, minX, maxY, minZ - DECAL_OFFSET, maxX, maxY, minZ - DECAL_OFFSET, packedLight, 0, 0, -1);
            case SOUTH -> quad(matrix, consumer, minX, minY, maxZ + DECAL_OFFSET, maxX, minY, maxZ + DECAL_OFFSET, maxX, maxY, maxZ + DECAL_OFFSET, minX, maxY, maxZ + DECAL_OFFSET, packedLight, 0, 0, 1);
            case WEST -> quad(matrix, consumer, minX - DECAL_OFFSET, minY, minZ, minX - DECAL_OFFSET, minY, maxZ, minX - DECAL_OFFSET, maxY, maxZ, minX - DECAL_OFFSET, maxY, minZ, packedLight, -1, 0, 0);
            case EAST -> quad(matrix, consumer, maxX + DECAL_OFFSET, minY, maxZ, maxX + DECAL_OFFSET, minY, minZ, maxX + DECAL_OFFSET, maxY, minZ, maxX + DECAL_OFFSET, maxY, maxZ, packedLight, 1, 0, 0);
            case UP -> quad(matrix, consumer, minX, maxY + DECAL_OFFSET, minZ, minX, maxY + DECAL_OFFSET, maxZ, maxX, maxY + DECAL_OFFSET, maxZ, maxX, maxY + DECAL_OFFSET, minZ, packedLight, 0, 1, 0);
            case DOWN -> quad(matrix, consumer, minX, minY - DECAL_OFFSET, maxZ, minX, minY - DECAL_OFFSET, minZ, maxX, minY - DECAL_OFFSET, minZ, maxX, minY - DECAL_OFFSET, maxZ, packedLight, 0, -1, 0);
        }
    }

    private static void quad(Matrix4f matrix, VertexConsumer consumer,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             int packedLight, int normalX, int normalY, int normalZ) {
        vertex(matrix, consumer, x1, y1, z1, 0, 1, packedLight, normalX, normalY, normalZ);
        vertex(matrix, consumer, x2, y2, z2, 1, 1, packedLight, normalX, normalY, normalZ);
        vertex(matrix, consumer, x3, y3, z3, 1, 0, packedLight, normalX, normalY, normalZ);
        vertex(matrix, consumer, x4, y4, z4, 0, 0, packedLight, normalX, normalY, normalZ);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float u, float v, int packedLight, int normalX, int normalY, int normalZ) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normalX, normalY, normalZ)
                .endVertex();
    }
}
