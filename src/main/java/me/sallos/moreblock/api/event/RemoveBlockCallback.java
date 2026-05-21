package me.sallos.moreblock.api.event;

import me.sallos.moreblock.config.ImportedBlockPacks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface RemoveBlockCallback {
    void onRemoveBlock(ImportedBlockPacks.Definition definition, Level level, BlockPos pos, BlockState state, BlockState newState, boolean movedByPiston);
}
