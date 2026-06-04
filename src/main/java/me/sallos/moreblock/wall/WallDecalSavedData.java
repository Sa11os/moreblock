package me.sallos.moreblock.wall;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WallDecalSavedData extends SavedData {
    private static final String DATA_NAME = "moreblock_wall_decals";
    private final List<WallDecalSystem.DecalPlacement> placements = new ArrayList<>();

    public static String dataName() {
        return DATA_NAME;
    }

    public static WallDecalSavedData load(CompoundTag tag) {
        WallDecalSavedData data = new WallDecalSavedData();
        ListTag list = tag.getList("decals", Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            CompoundTag decalTag = list.getCompound(index);
            Direction face = Direction.byName(decalTag.getString("face"));
            if (face == null) {
                continue;
            }
            ResourceLocation dimension = ResourceLocation.tryParse(decalTag.getString("dimension"));
            ResourceLocation texture = ResourceLocation.tryParse(decalTag.getString("texture"));
            if (dimension == null || texture == null) {
                continue;
            }
            data.placements.add(new WallDecalSystem.DecalPlacement(
                    dimension,
                    new BlockPos(decalTag.getInt("x"), decalTag.getInt("y"), decalTag.getInt("z")),
                    face,
                    texture
            ));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (WallDecalSystem.DecalPlacement placement : placements) {
            CompoundTag decalTag = new CompoundTag();
            decalTag.putString("dimension", placement.dimension().toString());
            decalTag.putInt("x", placement.pos().getX());
            decalTag.putInt("y", placement.pos().getY());
            decalTag.putInt("z", placement.pos().getZ());
            decalTag.putString("face", placement.face().getName());
            decalTag.putString("texture", placement.texture().toString());
            list.add(decalTag);
        }
        tag.put("decals", list);
        return tag;
    }

    public Collection<WallDecalSystem.DecalPlacement> placements() {
        return List.copyOf(placements);
    }

    public void replaceAll(Collection<WallDecalSystem.DecalPlacement> nextPlacements) {
        placements.clear();
        placements.addAll(nextPlacements);
        setDirty();
    }
}
