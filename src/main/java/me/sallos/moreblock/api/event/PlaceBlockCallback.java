package me.sallos.moreblock.api.event;

import me.sallos.moreblock.config.ImportedBlockPacks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface PlaceBlockCallback {
    void onPlaceBlock(ImportedBlockPacks.Definition definition, Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack);
}
