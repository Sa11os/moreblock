package me.sallos.moreblock.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.sallos.moreblock.Moreblock;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class GeoHitboxSystem {
    private GeoHitboxSystem() {
    }

    public static HorizontalShapes loadHorizontalShapes(Profile profile) {
        // 统一以北向为基准形状，再派生出四个水平朝向
        VoxelShape north = loadNorthShape(profile);
        return new HorizontalShapes(
                north,
                rotateY180(north),
                rotateYCounterClockwise90(north),
                rotateYClockwise90(north)
        );
    }

    public static HorizontalShapes loadHorizontalShapes(Path geoFilePath, Profile profile) {
        VoxelShape north = loadNorthShape(geoFilePath, profile);
        return new HorizontalShapes(
                north,
                rotateY180(north),
                rotateYCounterClockwise90(north),
                rotateYClockwise90(north)
        );
    }

    private static VoxelShape loadNorthShape(Profile profile) {
        try (InputStream inputStream = GeoHitboxSystem.class.getClassLoader().getResourceAsStream(profile.geoPath())) {
            if (inputStream == null) {
                Moreblock.LOGGER.warn("Geo Hitbox 资源未找到: {}", profile.geoPath());
                return Shapes.block();
            }
            return loadNorthShapeFromReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), profile, profile.geoPath());
        } catch (Exception exception) {
            Moreblock.LOGGER.error("Geo Hitbox 解析失败: {}", profile.geoPath(), exception);
            return Shapes.block();
        }
    }

    private static VoxelShape loadNorthShape(Path geoFilePath, Profile profile) {
        try (Reader reader = Files.newBufferedReader(geoFilePath, StandardCharsets.UTF_8)) {
            return loadNorthShapeFromReader(reader, profile, geoFilePath.toString());
        } catch (Exception exception) {
            Moreblock.LOGGER.error("Geo Hitbox 文件解析失败: {}", geoFilePath, exception);
            return Shapes.block();
        }
    }

    private static VoxelShape loadNorthShapeFromReader(Reader reader, Profile profile, String sourceDescription) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonArray geometries = root.getAsJsonArray("minecraft:geometry");
        if (geometries == null || geometries.size() == 0) {
            return Shapes.block();
        }

        JsonObject geometry = geometries.get(0).getAsJsonObject();
        JsonArray bones = geometry.getAsJsonArray("bones");
        if (bones == null) {
            return Shapes.block();
        }

        Map<String, JsonObject> boneMap = new LinkedHashMap<>();
        Map<String, List<JsonObject>> childrenMap = new LinkedHashMap<>();
        JsonObject rootBone = null;
        for (JsonElement boneElement : bones) {
            JsonObject bone = boneElement.getAsJsonObject();
            if (!bone.has("name")) {
                continue;
            }
            String boneName = bone.get("name").getAsString();
            boneMap.put(boneName, bone);
            if (bone.has("parent")) {
                childrenMap.computeIfAbsent(bone.get("parent").getAsString(), key -> new ArrayList<>()).add(bone);
            }
            if (profile.rootBoneName().equals(boneName)) {
                rootBone = bone;
            }
        }

        if (rootBone == null) {
            Moreblock.LOGGER.warn("Geo Hitbox 根骨骼未找到: {} -> {}", sourceDescription, profile.rootBoneName());
            return Shapes.block();
        }

        AtomicReference<VoxelShape> shapeRef = new AtomicReference<>(Shapes.empty());
        appendBoneShape(shapeRef, rootBone, boneMap, childrenMap, profile);
        VoxelShape shape = shapeRef.get();
        if (profile.mirrorX()) {
            shape = mirrorX(shape);
        }
        if (profile.mirrorZ()) {
            shape = mirrorZ(shape);
        }
        return shape.isEmpty() ? Shapes.block() : shape.optimize();
    }

    private static void appendBoneShape(AtomicReference<VoxelShape> shapeRef, JsonObject bone, Map<String, JsonObject> boneMap,
            Map<String, List<JsonObject>> childrenMap, Profile profile) {
        List<JsonObject> transformChain = buildTransformChain(bone, boneMap);
        JsonArray cubes = bone.getAsJsonArray("cubes");
        if (cubes != null) {
            for (JsonElement cubeElement : cubes) {
                JsonObject cube = cubeElement.getAsJsonObject();
                JsonArray origin = cube.getAsJsonArray("origin");
                JsonArray size = cube.getAsJsonArray("size");
                if (origin == null || size == null || origin.size() < 3 || size.size() < 3) {
                    continue;
                }

                double inflate = cube.has("inflate") ? cube.get("inflate").getAsDouble() : 0.0d;
                double minX = origin.get(0).getAsDouble() - inflate;
                double minY = origin.get(1).getAsDouble() - inflate;
                double minZ = origin.get(2).getAsDouble() - inflate;
                double maxX = origin.get(0).getAsDouble() + size.get(0).getAsDouble() + inflate;
                double maxY = origin.get(1).getAsDouble() + size.get(1).getAsDouble() + inflate;
                double maxZ = origin.get(2).getAsDouble() + size.get(2).getAsDouble() + inflate;

                Vec3[] corners = createCorners(minX, minY, minZ, maxX, maxY, maxZ);
                Vec3 cubePivot = cube.has("pivot")
                        ? readVec3(cube.getAsJsonArray("pivot"), new Vec3((minX + maxX) * 0.5d, (minY + maxY) * 0.5d, (minZ + maxZ) * 0.5d))
                        : new Vec3((minX + maxX) * 0.5d, (minY + maxY) * 0.5d, (minZ + maxZ) * 0.5d);
                Vec3 cubeRotation = readVec3(cube.getAsJsonArray("rotation"), new Vec3(0.0d, 0.0d, 0.0d));

                for (int index = 0; index < corners.length; index++) {
                    Vec3 point = rotateAroundPivot(corners[index], cubePivot, cubeRotation);
                    for (JsonObject transformBone : transformChain) {
                        Vec3 bonePivot = readVec3(transformBone.getAsJsonArray("pivot"), new Vec3(0.0d, 0.0d, 0.0d));
                        Vec3 boneRotation = readVec3(transformBone.getAsJsonArray("rotation"), new Vec3(0.0d, 0.0d, 0.0d));
                        point = rotateAroundPivot(point, bonePivot, boneRotation);
                    }
                    corners[index] = point;
                }

                Vec3 min = new Vec3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                Vec3 max = new Vec3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
                for (Vec3 corner : corners) {
                    min = new Vec3(Math.min(min.x, corner.x), Math.min(min.y, corner.y), Math.min(min.z, corner.z));
                    max = new Vec3(Math.max(max.x, corner.x), Math.max(max.y, corner.y), Math.max(max.z, corner.z));
                }

                // Bedrock 模型坐标中心在 X/Z 的 8,8，需要转成 Minecraft 方块局部坐标
                double blockMinX = (min.x + 8.0d) / 16.0d + profile.offsetX();
                double blockMinY = min.y / 16.0d + profile.offsetY();
                double blockMinZ = (min.z + 8.0d) / 16.0d + profile.offsetZ();
                double blockMaxX = (max.x + 8.0d) / 16.0d + profile.offsetX();
                double blockMaxY = max.y / 16.0d + profile.offsetY();
                double blockMaxZ = (max.z + 8.0d) / 16.0d + profile.offsetZ();
                shapeRef.set(Shapes.or(shapeRef.get(), Shapes.create(blockMinX, blockMinY, blockMinZ, blockMaxX, blockMaxY, blockMaxZ)));
            }
        }

        if (!profile.includeChildren()) {
            return;
        }
        String boneName = bone.has("name") ? bone.get("name").getAsString() : "";
        for (JsonObject child : childrenMap.getOrDefault(boneName, List.of())) {
            appendBoneShape(shapeRef, child, boneMap, childrenMap, profile);
        }
    }

    private static List<JsonObject> buildTransformChain(JsonObject bone, Map<String, JsonObject> boneMap) {
        List<JsonObject> chain = new ArrayList<>();
        JsonObject current = bone;
        while (current != null) {
            chain.add(current);
            if (!current.has("parent")) {
                break;
            }
            current = boneMap.get(current.get("parent").getAsString());
        }
        return chain;
    }

    private static Vec3[] createCorners(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new Vec3[] {
                new Vec3(minX, minY, minZ),
                new Vec3(minX, minY, maxZ),
                new Vec3(minX, maxY, minZ),
                new Vec3(minX, maxY, maxZ),
                new Vec3(maxX, minY, minZ),
                new Vec3(maxX, minY, maxZ),
                new Vec3(maxX, maxY, minZ),
                new Vec3(maxX, maxY, maxZ)
        };
    }

    private static Vec3 readVec3(JsonArray array, Vec3 defaultValue) {
        if (array == null || array.size() < 3) {
            return defaultValue;
        }
        return new Vec3(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
    }

    private static Vec3 rotateAroundPivot(Vec3 point, Vec3 pivot, Vec3 rotationDegrees) {
        if (rotationDegrees.x == 0.0d && rotationDegrees.y == 0.0d && rotationDegrees.z == 0.0d) {
            return point;
        }
        double px = point.x - pivot.x;
        double py = point.y - pivot.y;
        double pz = point.z - pivot.z;

        double rx = Math.toRadians(rotationDegrees.x);
        double ry = Math.toRadians(rotationDegrees.y);
        double rz = Math.toRadians(rotationDegrees.z);

        double cosX = Math.cos(rx);
        double sinX = Math.sin(rx);
        double ny = py * cosX - pz * sinX;
        double nz = py * sinX + pz * cosX;
        py = ny;
        pz = nz;

        double cosY = Math.cos(ry);
        double sinY = Math.sin(ry);
        double nx = px * cosY + pz * sinY;
        nz = -px * sinY + pz * cosY;
        px = nx;
        pz = nz;

        double cosZ = Math.cos(rz);
        double sinZ = Math.sin(rz);
        nx = px * cosZ - py * sinZ;
        ny = px * sinZ + py * cosZ;
        px = nx;
        py = ny;

        return new Vec3(px + pivot.x, py + pivot.y, pz + pivot.z);
    }

    private static VoxelShape rotateYClockwise90(VoxelShape source) {
        AtomicReference<VoxelShape> rotated = new AtomicReference<>(Shapes.empty());
        source.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                rotated.set(Shapes.or(rotated.get(), Shapes.create(1.0d - maxZ, minY, minX, 1.0d - minZ, maxY, maxX))));
        return rotated.get().optimize();
    }

    private static VoxelShape rotateYCounterClockwise90(VoxelShape source) {
        AtomicReference<VoxelShape> rotated = new AtomicReference<>(Shapes.empty());
        source.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                rotated.set(Shapes.or(rotated.get(), Shapes.create(minZ, minY, 1.0d - maxX, maxZ, maxY, 1.0d - minX))));
        return rotated.get().optimize();
    }

    private static VoxelShape rotateY180(VoxelShape source) {
        AtomicReference<VoxelShape> rotated = new AtomicReference<>(Shapes.empty());
        source.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                rotated.set(Shapes.or(rotated.get(), Shapes.create(1.0d - maxX, minY, 1.0d - maxZ, 1.0d - minX, maxY, 1.0d - minZ))));
        return rotated.get().optimize();
    }

    private static VoxelShape mirrorX(VoxelShape source) {
        AtomicReference<VoxelShape> mirrored = new AtomicReference<>(Shapes.empty());
        source.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                mirrored.set(Shapes.or(mirrored.get(), Shapes.create(1.0d - maxX, minY, minZ, 1.0d - minX, maxY, maxZ))));
        return mirrored.get().optimize();
    }

    private static VoxelShape mirrorZ(VoxelShape source) {
        AtomicReference<VoxelShape> mirrored = new AtomicReference<>(Shapes.empty());
        source.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                mirrored.set(Shapes.or(mirrored.get(), Shapes.create(minX, minY, 1.0d - maxZ, maxX, maxY, 1.0d - minZ))));
        return mirrored.get().optimize();
    }

    public record Profile(
            String geoPath,
            String rootBoneName,
            boolean includeChildren,
            boolean mirrorX,
            boolean mirrorZ,
            double offsetX,
            double offsetY,
            double offsetZ
    ) {
    }

    public record HorizontalShapes(VoxelShape north, VoxelShape south, VoxelShape west, VoxelShape east) {
        public static HorizontalShapes ofFullBlock() {
            return new HorizontalShapes(Shapes.block(), Shapes.block(), Shapes.block(), Shapes.block());
        }

        public VoxelShape get(Direction facing) {
            return switch (facing) {
                case SOUTH -> south;
                case WEST -> west;
                case EAST -> east;
                default -> north;
            };
        }
    }

    private record Vec3(double x, double y, double z) {
    }
}
