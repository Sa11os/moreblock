package me.sallos.moreblock.entity;

import me.sallos.moreblock.Moreblock;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

@SuppressWarnings("null")
final class ImportedEnderDragonPhaseBoss {
    private static final String ANIMATION_FLY_LOOP = "fly_loop";
    private static final String ANIMATION_FLY_BANK = "fly_bank";
    private static final String ANIMATION_LANDING_APPROACH = "landing_approach";
    private static final String ANIMATION_PERCH_SCAN = "perch_scan";
    private static final String ANIMATION_TAKEOFF_PUSH = "takeoff_push";
    private static final String ANIMATION_DEATH_FINISH = "death_finish";
    private static final double HOLDING_RADIUS = 18.0d;
    private static final double LANDING_RADIUS = 5.0d;
    private static final int LANDING_APPROACH_TICKS = 50;
    private static final int PERCH_SCAN_TICKS = 100;
    private static final int TAKEOFF_PUSH_TICKS = 26;

    private final ImportedEntity entity;
    private final ServerBossEvent bossBar;

    private Phase phase = Phase.HOLDING_PATTERN;
    private int phaseTicks;
    private double orbitAngle;
    private int lastHurtTimestamp;

    ImportedEnderDragonPhaseBoss(ImportedEntity entity) {
        this.entity = entity;
        this.bossBar = new ServerBossEvent(
                entity.getDisplayName(),
                BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarOverlay.PROGRESS
        );
        this.bossBar.setDarkenScreen(true);
        this.orbitAngle = entity.getRandom().nextDouble() * Math.PI * 2.0d;
        applyPhase(Phase.HOLDING_PATTERN);
    }

    void tick() {
        bossBar.setName(entity.getDisplayName());
        bossBar.setProgress(Math.max(0.0f, Math.min(1.0f, entity.getHealth() / entity.getMaxHealth())));
        if (!entity.isAlive()) {
            applyPhase(Phase.DEATH);
            return;
        }

        if (entity.hurtTime > 0) {
            lastHurtTimestamp = entity.tickCount;
        }

        phaseTicks++;
        LivingEntity target = findTargetPlayer();
        switch (phase) {
            case HOLDING_PATTERN -> tickHoldingPattern(target);
            case LANDING_APPROACH -> tickLandingApproach();
            case PERCH_SCAN -> tickPerchScan(target);
            case TAKEOFF -> tickTakeoff();
            case DEATH -> tickDeath();
        }
    }

    void startSeenByPlayer(ServerPlayer player) {
        bossBar.addPlayer(player);
    }

    void stopSeenByPlayer(ServerPlayer player) {
        bossBar.removePlayer(player);
    }

    private void tickHoldingPattern(LivingEntity target) {
        orbitAngle += 0.035d;
        Vec3 center = portalCenter();
        Vec3 destination = new Vec3(
                center.x + Math.cos(orbitAngle) * HOLDING_RADIUS,
                center.y + 10.0d + Math.sin(orbitAngle * 0.5d) * 3.0d,
                center.z + Math.sin(orbitAngle) * HOLDING_RADIUS
        );
        moveTowards(destination, 1.05d, 0.12d);

        Vec3 velocity = entity.getDeltaMovement();
        boolean banking = Math.abs(velocity.x) + Math.abs(velocity.z) > 0.08d
                && Math.abs(wrapDegrees(entity.getYRot() - entity.yBodyRot)) > 10.0f;
        entity.setForcedAnimation(banking ? ANIMATION_FLY_BANK : ANIMATION_FLY_LOOP, true);

        if (phaseTicks > 90 && target != null && entity.distanceToSqr(center.x, center.y, center.z) > 64.0d) {
            applyPhase(Phase.LANDING_APPROACH);
        }
    }

    private void tickLandingApproach() {
        Vec3 landingTarget = portalCenter().add(0.0d, 2.0d, 0.0d);
        moveTowards(landingTarget, 0.85d, 0.10d);
        entity.setForcedAnimation(ANIMATION_LANDING_APPROACH, true);

        if (entity.position().distanceTo(landingTarget) <= LANDING_RADIUS || phaseTicks >= LANDING_APPROACH_TICKS) {
            entity.setPos(landingTarget.x, landingTarget.y, landingTarget.z);
            entity.setDeltaMovement(Vec3.ZERO);
            applyPhase(Phase.PERCH_SCAN);
        }
    }

    private void tickPerchScan(LivingEntity target) {
        Vec3 perchPos = portalCenter().add(0.0d, 2.0d, 0.0d);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setPos(perchPos.x, perchPos.y, perchPos.z);
        entity.setForcedAnimation(ANIMATION_PERCH_SCAN, true);
        if (target != null) {
            lookAtTarget(target);
        }

        boolean tookHeavyPressure = entity.tickCount - lastHurtTimestamp < 20;
        if (phaseTicks >= PERCH_SCAN_TICKS || tookHeavyPressure) {
            applyPhase(Phase.TAKEOFF);
        }
    }

    private void tickTakeoff() {
        entity.setForcedAnimation(ANIMATION_TAKEOFF_PUSH, false);
        entity.setDeltaMovement(entity.getDeltaMovement().scale(0.7d).add(0.0d, 0.08d, 0.0d));
        if (phaseTicks >= TAKEOFF_PUSH_TICKS) {
            orbitAngle = Math.atan2(entity.getZ() - portalCenter().z, entity.getX() - portalCenter().x);
            applyPhase(Phase.HOLDING_PATTERN);
        }
    }

    private void tickDeath() {
        entity.setForcedAnimation(ANIMATION_DEATH_FINISH, false);
        entity.setDeltaMovement(Vec3.ZERO);
    }

    private void applyPhase(Phase newPhase) {
        if (phase == newPhase && phaseTicks > 0) {
            return;
        }
        phase = newPhase;
        phaseTicks = 0;
        entity.setBossPhaseId(newPhase.id);
        Moreblock.LOGGER.info(
                "末影龙AI阶段切换: entity={}, phase={}, phaseId={}, pos=({}, {}, {}), health={}/{}",
                entity.getStringUUID(),
                newPhase.name(),
                newPhase.id,
                round(entity.getX()),
                round(entity.getY()),
                round(entity.getZ()),
                round(entity.getHealth()),
                round(entity.getMaxHealth())
        );
        switch (newPhase) {
            case HOLDING_PATTERN -> entity.setForcedAnimation(ANIMATION_FLY_LOOP, true);
            case LANDING_APPROACH -> entity.setForcedAnimation(ANIMATION_LANDING_APPROACH, true);
            case PERCH_SCAN -> entity.setForcedAnimation(ANIMATION_PERCH_SCAN, true);
            case TAKEOFF -> entity.setForcedAnimation(ANIMATION_TAKEOFF_PUSH, false);
            case DEATH -> entity.setForcedAnimation(ANIMATION_DEATH_FINISH, false);
        }
    }

    private LivingEntity findTargetPlayer() {
        return entity.level().players().stream()
                .filter(player -> player.isAlive() && player.distanceToSqr(entity) <= 192.0d * 192.0d)
                .min(Comparator.comparingDouble(player -> player.distanceToSqr(entity)))
                .orElse(null);
    }

    private void moveTowards(Vec3 destination, double speed, double verticalScale) {
        Vec3 direction = destination.subtract(entity.position());
        if (direction.lengthSqr() < 1.0E-4d) {
            entity.setDeltaMovement(entity.getDeltaMovement().scale(0.8d));
            return;
        }

        Vec3 normalized = direction.normalize();
        double flySpeed = Math.max(0.08d, entity.getAttributeValue(Attributes.FLYING_SPEED));
        Vec3 desiredVelocity = new Vec3(
                normalized.x * flySpeed * speed,
                normalized.y * flySpeed * speed * verticalScale,
                normalized.z * flySpeed * speed
        );
        entity.setDeltaMovement(entity.getDeltaMovement().scale(0.82d).add(desiredVelocity.scale(0.18d)));
        entity.getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, speed);
        float targetYaw = (float) (Math.toDegrees(Math.atan2(normalized.z, normalized.x)) - 90.0d);
        entity.setYRot(rotlerp(entity.getYRot(), targetYaw, 8.0f));
        entity.yBodyRot = entity.getYRot();
        entity.yHeadRot = entity.getYRot();
    }

    private void lookAtTarget(LivingEntity target) {
        double dx = target.getX() - entity.getX();
        double dz = target.getZ() - entity.getZ();
        float targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0d);
        entity.setYRot(rotlerp(entity.getYRot(), targetYaw, 10.0f));
        entity.yBodyRot = entity.getYRot();
        entity.yHeadRot = entity.getYRot();
    }

    private Vec3 portalCenter() {
        return new Vec3(0.5d, Math.max(entity.level().getMinBuildHeight() + 8.0d, 64.0d), 0.5d);
    }

    private static float rotlerp(float current, float target, float maxDelta) {
        float delta = wrapDegrees(target - current);
        if (delta > maxDelta) {
            delta = maxDelta;
        }
        if (delta < -maxDelta) {
            delta = -maxDelta;
        }
        return current + delta;
    }

    private static float wrapDegrees(float value) {
        float wrapped = value % 360.0f;
        if (wrapped >= 180.0f) {
            wrapped -= 360.0f;
        }
        if (wrapped < -180.0f) {
            wrapped += 360.0f;
        }
        return wrapped;
    }

    private static String round(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }

    private enum Phase {
        HOLDING_PATTERN(0),
        LANDING_APPROACH(2),
        PERCH_SCAN(6),
        TAKEOFF(4),
        DEATH(9);

        private final int id;

        Phase(int id) {
            this.id = id;
        }
    }
}
