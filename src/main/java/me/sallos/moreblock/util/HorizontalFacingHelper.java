package me.sallos.moreblock.util;

import net.minecraft.core.Direction;

public final class HorizontalFacingHelper {
    private HorizontalFacingHelper() {
    }

    public static Direction rotateClockwise(Direction facing, int steps) {
        int normalizedSteps = Math.floorMod(steps, 4);
        for (int step = 0; step < normalizedSteps; step++) {
            facing = facing.getClockWise();
        }
        return facing;
    }
}
