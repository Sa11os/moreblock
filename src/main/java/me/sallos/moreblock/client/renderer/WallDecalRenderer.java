package me.sallos.moreblock.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Moreblock.MODID, value = Dist.CLIENT)
public final class WallDecalRenderer {
    private static final float DECAL_OFFSET = 0.002f;
    private static final Map<ResourceLocation, DecalSize> DECAL_SIZES = new ConcurrentHashMap<>();

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
            renderFace(level, matrix, consumer, placement.pos(), placement.face(), resolveDecalSize(placement.texture()));
        }
        poseStack.popPose();
        bufferSource.endBatch();
    }

    private static void renderFace(Level level, Matrix4f matrix, VertexConsumer consumer, BlockPos pos, Direction face, DecalSize size) {
        int packedLight = LevelRenderer.getLightColor(level, pos.relative(face));
        float minX = pos.getX();
        float minY = pos.getY();
        float minZ = pos.getZ();
        float maxX = minX + 1.0f;
        float maxY = minY + 1.0f;
        float maxZ = minZ + 1.0f;
        float horizontalSize = size.horizontalSize();
        float verticalSize = size.verticalSize();
        float horizontalInset = (1.0f - horizontalSize) * 0.5f;
        float verticalInset = (1.0f - verticalSize) * 0.5f;
        float faceMinX = minX + horizontalInset;
        float faceMaxX = maxX - horizontalInset;
        float faceMinY = minY + verticalInset;
        float faceMaxY = maxY - verticalInset;
        float faceMinZ = minZ + horizontalInset;
        float faceMaxZ = maxZ - horizontalInset;

        switch (face) {
            case NORTH -> quad(matrix, consumer, faceMaxX, faceMinY, minZ - DECAL_OFFSET, faceMinX, faceMinY, minZ - DECAL_OFFSET, faceMinX, faceMaxY, minZ - DECAL_OFFSET, faceMaxX, faceMaxY, minZ - DECAL_OFFSET, packedLight, 0, 0, -1);
            case SOUTH -> quad(matrix, consumer, faceMinX, faceMinY, maxZ + DECAL_OFFSET, faceMaxX, faceMinY, maxZ + DECAL_OFFSET, faceMaxX, faceMaxY, maxZ + DECAL_OFFSET, faceMinX, faceMaxY, maxZ + DECAL_OFFSET, packedLight, 0, 0, 1);
            case WEST -> quad(matrix, consumer, minX - DECAL_OFFSET, faceMinY, faceMinZ, minX - DECAL_OFFSET, faceMinY, faceMaxZ, minX - DECAL_OFFSET, faceMaxY, faceMaxZ, minX - DECAL_OFFSET, faceMaxY, faceMinZ, packedLight, -1, 0, 0);
            case EAST -> quad(matrix, consumer, maxX + DECAL_OFFSET, faceMinY, faceMaxZ, maxX + DECAL_OFFSET, faceMinY, faceMinZ, maxX + DECAL_OFFSET, faceMaxY, faceMinZ, maxX + DECAL_OFFSET, faceMaxY, faceMaxZ, packedLight, 1, 0, 0);
            case UP -> quad(matrix, consumer, faceMinX, maxY + DECAL_OFFSET, faceMinZ, faceMinX, maxY + DECAL_OFFSET, faceMaxZ, faceMaxX, maxY + DECAL_OFFSET, faceMaxZ, faceMaxX, maxY + DECAL_OFFSET, faceMinZ, packedLight, 0, 1, 0);
            case DOWN -> quad(matrix, consumer, faceMinX, minY - DECAL_OFFSET, faceMaxZ, faceMinX, minY - DECAL_OFFSET, faceMinZ, faceMaxX, minY - DECAL_OFFSET, faceMinZ, faceMaxX, minY - DECAL_OFFSET, faceMaxZ, packedLight, 0, -1, 0);
        }
    }

    private static DecalSize resolveDecalSize(ResourceLocation textureLocation) {
        return DECAL_SIZES.computeIfAbsent(textureLocation, WallDecalRenderer::loadDecalSize);
    }

    private static DecalSize loadDecalSize(ResourceLocation textureLocation) {
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResource(textureLocation).orElse(null);
            if (resource == null) {
                Moreblock.LOGGER.warn("未找到墙纸贴画纹理资源: {}", textureLocation);
                return DecalSize.square();
            }
            try (InputStream inputStream = resource.open(); NativeImage image = NativeImage.read(inputStream)) {
                if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                    Moreblock.LOGGER.warn("墙纸贴画纹理尺寸无效，使用默认比例: {}", textureLocation);
                    return DecalSize.square();
                }
                return DecalSize.fromPixels(image.getWidth(), image.getHeight());
            }
        } catch (IOException exception) {
            Moreblock.LOGGER.warn("读取墙纸贴画尺寸失败: {}", textureLocation, exception);
            return DecalSize.square();
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

    private record DecalSize(float horizontalSize, float verticalSize) {
        private static DecalSize square() {
            return new DecalSize(1.0f, 1.0f);
        }

        private static DecalSize fromPixels(int width, int height) {
            if (width <= 0 || height <= 0) {
                return square();
            }
            if (width >= height) {
                return new DecalSize(1.0f, (float) height / (float) width);
            }
            return new DecalSize((float) width / (float) height, 1.0f);
        }
    }
}
