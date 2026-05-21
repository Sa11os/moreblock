package me.sallos.moreblock.api.event;

import net.minecraft.world.InteractionResult;

public enum MoreBlockInteractionResult {
    PASS,
    SUCCESS,
    CONSUME,
    FAIL;

    public InteractionResult toMinecraftResult() {
        return switch (this) {
            case SUCCESS -> InteractionResult.SUCCESS;
            case CONSUME -> InteractionResult.CONSUME;
            case FAIL -> InteractionResult.FAIL;
            default -> InteractionResult.PASS;
        };
    }
}
