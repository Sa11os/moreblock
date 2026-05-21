package me.sallos.moreblock.api.event;

import me.sallos.moreblock.config.ImportedBlockPacks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public final class MoreBlockEvents {
    private static final List<UseBlockCallback> USE_BLOCK_CALLBACKS = new ArrayList<>();
    private static final List<PlaceBlockCallback> PLACE_BLOCK_CALLBACKS = new ArrayList<>();
    private static final List<RemoveBlockCallback> REMOVE_BLOCK_CALLBACKS = new ArrayList<>();

    private MoreBlockEvents() {
    }

    public static void onUseBlock(UseBlockCallback callback) {
        USE_BLOCK_CALLBACKS.add(Objects.requireNonNull(callback, "callback"));
    }

    public static void onPlaceBlock(PlaceBlockCallback callback) {
        PLACE_BLOCK_CALLBACKS.add(Objects.requireNonNull(callback, "callback"));
    }

    public static void onRemoveBlock(RemoveBlockCallback callback) {
        REMOVE_BLOCK_CALLBACKS.add(Objects.requireNonNull(callback, "callback"));
    }

    public static MoreBlockInteractionResult fireUseBlock(ImportedBlockPacks.Definition definition, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        for (UseBlockCallback callback : List.copyOf(USE_BLOCK_CALLBACKS)) {
            MoreBlockInteractionResult result = callback.onUseBlock(definition, state, level, pos, player, hand, hit);
            if (result != null && result != MoreBlockInteractionResult.PASS) {
                return result;
            }
        }
        return MoreBlockInteractionResult.PASS;
    }

    public static void firePlaceBlock(ImportedBlockPacks.Definition definition, Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        for (PlaceBlockCallback callback : List.copyOf(PLACE_BLOCK_CALLBACKS)) {
            callback.onPlaceBlock(definition, level, pos, state, placer, stack);
        }
    }

    public static void fireRemoveBlock(ImportedBlockPacks.Definition definition, Level level, BlockPos pos, BlockState state, BlockState newState, boolean movedByPiston) {
        for (RemoveBlockCallback callback : List.copyOf(REMOVE_BLOCK_CALLBACKS)) {
            callback.onRemoveBlock(definition, level, pos, state, newState, movedByPiston);
        }
    }
}
