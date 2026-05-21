package me.sallos.moreblock.api.event;

import me.sallos.moreblock.config.ImportedBlockPacks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@FunctionalInterface
public interface UseBlockCallback {
    MoreBlockInteractionResult onUseBlock(ImportedBlockPacks.Definition definition, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit);
}
