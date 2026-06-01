package me.sallos.moreblock.entity;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedEntityPacks;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

@SuppressWarnings("null")
public class ImportedEntity extends PathfinderMob implements GeoEntity, RangedAttackMob {
    private static final ResourceLocation ENDER_DRAGON_AI_TEMPLATE = ResourceLocation.fromNamespaceAndPath("minecraft", "ender_dragon");
    private static final EntityDataAccessor<String> DATA_FORCED_ANIMATION = SynchedEntityData.defineId(ImportedEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_FORCED_ANIMATION_LOOP = SynchedEntityData.defineId(ImportedEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_BOSS_PHASE = SynchedEntityData.defineId(ImportedEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ImportedEntityPacks.EntityAnimationState activeAnimationState = ImportedEntityPacks.EntityAnimationState.IDLE;
    private String activeAnimationName;
    private long nextAnimationSwitchTick;
    private boolean playingForcedAnimation;
    private boolean activeForcedAnimationLoop;
    private ImportedEnderDragonPhaseBoss enderDragonPhaseBoss;
    private String lastLoggedForcedAnimation;
    private String lastAppliedForcedAnimation;
    private ImportedEntityPacks.EntityAnimationState lastLoggedDesiredAnimationState;
    private String lastLoggedDesiredAnimationName;

    public ImportedEntity(EntityType<? extends ImportedEntity> entityType, Level level) {
        super(entityType, level);
        ensureSpecialRuntimeInitialized();
    }

    public static AttributeSupplier.Builder createAttributes(ImportedEntityPacks.Definition definition) {
        ImportedEntityPacks.Definition resolvedDefinition = definition == null
                ? new ImportedEntityPacks.Definition(
                "moreblock",
                "fallback",
                "Fallback",
                "Fallback",
                "fallback",
                null,
                null,
                null,
                null,
                null,
                null,
                0.6f,
                1.8f,
                1.53f,
                20.0d,
                0.2d,
                16.0d,
                2.0d,
                0.0d,
                0.2d,
                8,
                3,
                true,
                null,
                true,
                false,
                0,
                0,
                true,
                true,
                null,
                null,
                null,
                ImportedEntityPacks.AnimationProfile.defaultProfile())
                : definition;
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, resolvedDefinition.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, resolvedDefinition.movementSpeed())
                .add(Attributes.FLYING_SPEED, Math.max(0.08d, resolvedDefinition.movementSpeed()))
                .add(Attributes.FOLLOW_RANGE, resolvedDefinition.followRange())
                .add(Attributes.ATTACK_DAMAGE, resolvedDefinition.attackDamage())
                .add(Attributes.ARMOR, resolvedDefinition.armor())
                .add(Attributes.KNOCKBACK_RESISTANCE, resolvedDefinition.knockbackResistance());
    }

    public ImportedEntityPacks.Definition getDefinition() {
        return ImportedEntityPacks.getDefinition(getType());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        ensureSpecialRuntimeInitialized();
        ImportedEntityPacks.Definition definition = getDefinition();
        if (definition == null || !definition.aiEnabled()) {
            return;
        }
        if (!ImportedEntityAiTemplates.apply(this, definition)) {
            addIdleGoals();
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        // 导入实体默认更偏装饰用途，基础版先按常驻实体处理，避免离远后被自然清理。
        return false;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        ImportedEntityPacks.Definition definition = getDefinition();
        return definition == null ? super.getStandingEyeHeight(pose, dimensions) : definition.eyeHeight();
    }

    public void addGoal(int priority, Goal goal) {
        goalSelector.addGoal(priority, goal);
    }

    public void addTargetGoal(int priority, Goal goal) {
        targetSelector.addGoal(priority, goal);
    }

    public void addIdleGoals() {
        addGoal(7, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 1.0d));
        addGoal(8, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 6.0f));
        addGoal(9, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (level().isClientSide) {
            return;
        }

        Arrow arrow = new Arrow(level(), this);
        double deltaX = target.getX() - getX();
        double deltaY = target.getY(0.3333333333333333d) - arrow.getY();
        double deltaZ = target.getZ() - getZ();
        double horizontalDistance = Mth.sqrt((float) (deltaX * deltaX + deltaZ * deltaZ));
        arrow.setBaseDamage(Math.max(1.0d, getAttributeValue(Attributes.ATTACK_DAMAGE)));
        arrow.shoot(deltaX, deltaY + horizontalDistance * 0.2d, deltaZ, 1.6f, 8.0f);
        level().addFreshEntity(arrow);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        if (isEnderDragonTemplate()) {
            FlyingPathNavigation flyingNavigation = new FlyingPathNavigation(this, level);
            flyingNavigation.setCanOpenDoors(false);
            flyingNavigation.setCanFloat(true);
            return flyingNavigation;
        }
        return super.createNavigation(level);
    }

    @Override
    public void tick() {
        ensureSpecialRuntimeInitialized();
        if (!level().isClientSide && enderDragonPhaseBoss != null) {
            enderDragonPhaseBoss.tick();
        }
        super.tick();
        if (isEnderDragonTemplate()) {
            setNoGravity(true);
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (isEnderDragonTemplate()) {
            moveRelative(0.02f, travelVector);
            move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.91d));
            return;
        }
        super.travel(travelVector);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos) {
        if (isEnderDragonTemplate()) {
            return;
        }
        super.checkFallDamage(y, onGround, state, pos);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);
        if (enderDragonPhaseBoss != null) {
            enderDragonPhaseBoss.startSeenByPlayer(serverPlayer);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer serverPlayer) {
        super.stopSeenByPlayer(serverPlayer);
        if (enderDragonPhaseBoss != null) {
            enderDragonPhaseBoss.stopSeenByPlayer(serverPlayer);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        ImportedEntityPacks.Definition definition = getDefinition();
        int transitionTicks = definition == null || definition.animationTransition() ? 5 : 0;
        controllers.add(new AnimationController<>(this, "main", transitionTicks, this::updateAnimation));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private PlayState updateAnimation(AnimationState<ImportedEntity> state) {
        String forcedAnimationName = getForcedAnimationName();
        if (!forcedAnimationName.isEmpty()) {
            boolean forcedLoop = isForcedAnimationLoop();
            boolean shouldSwitchForcedAnimation = !playingForcedAnimation
                    || activeAnimationName == null
                    || !activeAnimationName.equals(forcedAnimationName)
                    || activeForcedAnimationLoop != forcedLoop;
            if (!shouldSwitchForcedAnimation) {
                return PlayState.CONTINUE;
            }

            playingForcedAnimation = true;
            activeAnimationName = forcedAnimationName;
            activeForcedAnimationLoop = forcedLoop;
            nextAnimationSwitchTick = Long.MAX_VALUE;
            if (!isAnimationTransitionEnabled()) {
                state.getController().forceAnimationReset();
            }
            if (forcedLoop) {
                state.setAnimation(RawAnimation.begin().thenLoop(forcedAnimationName));
            } else {
                state.setAnimation(RawAnimation.begin().thenPlay(forcedAnimationName));
            }
            logEnderDragonAnimationTrigger("强制动画已应用", forcedAnimationName, forcedLoop, null);
            return PlayState.CONTINUE;
        }

        if (playingForcedAnimation) {
            playingForcedAnimation = false;
            activeForcedAnimationLoop = false;
            activeAnimationName = null;
            if (!isAnimationTransitionEnabled()) {
                state.getController().forceAnimationReset();
            }
        }

        ImportedEntityPacks.Definition definition = getDefinition();
        ImportedEntityPacks.AnimationProfile profile = definition == null
                ? ImportedEntityPacks.AnimationProfile.defaultProfile()
                : definition.animationProfile();
        ImportedEntityPacks.EntityAnimationState desiredState = resolveDesiredAnimationState(state);
        ImportedEntityPacks.AnimationOption option = selectAnimationOption(profile, desiredState);
        if (option == null) {
            return PlayState.STOP;
        }

        boolean shouldSwitch = desiredState != activeAnimationState
                || activeAnimationName == null
                || tickCount >= nextAnimationSwitchTick;
        if (!shouldSwitch) {
            return PlayState.CONTINUE;
        }

        activeAnimationState = desiredState;
        activeAnimationName = option.animationName();
        nextAnimationSwitchTick = desiredState == ImportedEntityPacks.EntityAnimationState.DIE
                ? Long.MAX_VALUE
                : (long) tickCount + Math.max(1, option.durationTicks());

        if (!isAnimationTransitionEnabled()) {
            state.getController().forceAnimationReset();
        }
        if (option.playback() == ImportedEntityPacks.AnimationPlayback.LOOP) {
            state.setAnimation(RawAnimation.begin().thenLoop(option.animationName()));
        } else {
            state.setAnimation(RawAnimation.begin().thenPlay(option.animationName()));
        }
        logEnderDragonAnimationTrigger(
                "常规动画已应用",
                option.animationName(),
                option.playback() == ImportedEntityPacks.AnimationPlayback.LOOP,
                desiredState
        );
        return PlayState.CONTINUE;
    }

    private boolean isAnimationTransitionEnabled() {
        ImportedEntityPacks.Definition definition = getDefinition();
        return definition == null || definition.animationTransition();
    }

    private ImportedEntityPacks.EntityAnimationState resolveDesiredAnimationState(AnimationState<ImportedEntity> state) {
        ImportedEntityPacks.AnimationProfile profile = getDefinition() == null
                ? ImportedEntityPacks.AnimationProfile.defaultProfile()
                : getDefinition().animationProfile();
        if (isDeadOrDying()) {
            return ImportedEntityPacks.EntityAnimationState.DIE;
        }
        if (hurtTime > 0 && profile.hasState(ImportedEntityPacks.EntityAnimationState.HURT)) {
            return ImportedEntityPacks.EntityAnimationState.HURT;
        }
        if (tickCount <= resolveStateDuration(profile, ImportedEntityPacks.EntityAnimationState.SPAWN)
                && profile.hasState(ImportedEntityPacks.EntityAnimationState.SPAWN)) {
            return ImportedEntityPacks.EntityAnimationState.SPAWN;
        }
        if (swinging || attackAnim > 0.05f) {
            return ImportedEntityPacks.EntityAnimationState.ATTACK;
        }
        if (state.isMoving()) {
            if (profile.hasState(ImportedEntityPacks.EntityAnimationState.RUN) && isRunningAnimationState()) {
                return ImportedEntityPacks.EntityAnimationState.RUN;
            }
            return ImportedEntityPacks.EntityAnimationState.WALK;
        }
        return ImportedEntityPacks.EntityAnimationState.IDLE;
    }

    private boolean isRunningAnimationState() {
        double horizontalSpeed = getDeltaMovement().horizontalDistance();
        double configuredSpeed = getAttributeValue(Attributes.MOVEMENT_SPEED);
        return horizontalSpeed >= 0.08d || configuredSpeed >= 0.32d;
    }

    private int resolveStateDuration(
            ImportedEntityPacks.AnimationProfile profile,
            ImportedEntityPacks.EntityAnimationState animationState
    ) {
        int maxDuration = 0;
        for (ImportedEntityPacks.AnimationOption option : profile.optionsFor(animationState)) {
            maxDuration = Math.max(maxDuration, option.durationTicks());
        }
        return maxDuration;
    }

    private ImportedEntityPacks.AnimationOption selectAnimationOption(
            ImportedEntityPacks.AnimationProfile profile,
            ImportedEntityPacks.EntityAnimationState state
    ) {
        java.util.List<ImportedEntityPacks.AnimationOption> options = profile.optionsFor(state);
        if (options.isEmpty()) {
            return null;
        }
        if (options.size() == 1) {
            return options.get(0);
        }

        double totalWeight = 0.0d;
        for (ImportedEntityPacks.AnimationOption option : options) {
            totalWeight += Math.max(0.0d, option.weight());
        }
        if (totalWeight <= 0.0d) {
            return options.get(0);
        }

        double randomValue = random.nextDouble() * totalWeight;
        double currentWeight = 0.0d;
        for (ImportedEntityPacks.AnimationOption option : options) {
            currentWeight += Math.max(0.0d, option.weight());
            if (randomValue <= currentWeight) {
                return option;
            }
        }
        return options.get(options.size() - 1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_FORCED_ANIMATION, "");
        entityData.define(DATA_FORCED_ANIMATION_LOOP, false);
        entityData.define(DATA_BOSS_PHASE, 0);
    }

    public void setForcedAnimation(String animationName, boolean loop) {
        if (animationName == null || animationName.isBlank()) {
            clearForcedAnimation();
            return;
        }
        String previousAnimation = entityData.get(DATA_FORCED_ANIMATION);
        boolean previousLoop = entityData.get(DATA_FORCED_ANIMATION_LOOP);
        entityData.set(DATA_FORCED_ANIMATION, animationName);
        entityData.set(DATA_FORCED_ANIMATION_LOOP, loop);
        if (isEnderDragonTemplate() && (!animationName.equals(previousAnimation) || loop != previousLoop)) {
            Moreblock.LOGGER.info(
                    "末影龙AI强制动画切换: entity={}, phaseId={}, animation={}, loop={}, tick={}, pos=({}, {}, {}), speed=({}, {}, {})",
                    getStringUUID(),
                    getBossPhaseId(),
                    animationName,
                    loop,
                    tickCount,
                    round(getX()),
                    round(getY()),
                    round(getZ()),
                    round(getDeltaMovement().x),
                    round(getDeltaMovement().y),
                    round(getDeltaMovement().z)
            );
            lastLoggedForcedAnimation = animationName + "|" + loop;
        }
    }

    public void clearForcedAnimation() {
        String previousAnimation = entityData.get(DATA_FORCED_ANIMATION);
        entityData.set(DATA_FORCED_ANIMATION, "");
        entityData.set(DATA_FORCED_ANIMATION_LOOP, false);
        if (isEnderDragonTemplate() && !previousAnimation.isEmpty()) {
            Moreblock.LOGGER.info(
                    "末影龙AI强制动画清除: entity={}, phaseId={}, tick={}",
                    getStringUUID(),
                    getBossPhaseId(),
                    tickCount
            );
            lastLoggedForcedAnimation = null;
        }
    }

    public String getForcedAnimationName() {
        return entityData.get(DATA_FORCED_ANIMATION);
    }

    public boolean isForcedAnimationLoop() {
        return entityData.get(DATA_FORCED_ANIMATION_LOOP);
    }

    public void setBossPhaseId(int phaseId) {
        entityData.set(DATA_BOSS_PHASE, phaseId);
    }

    public int getBossPhaseId() {
        return entityData.get(DATA_BOSS_PHASE);
    }

    public boolean isEnderDragonTemplate() {
        ImportedEntityPacks.Definition definition = getDefinition();
        return definition != null && ENDER_DRAGON_AI_TEMPLATE.equals(definition.aiTemplate());
    }

    private void ensureSpecialRuntimeInitialized() {
        if (!isEnderDragonTemplate()) {
            return;
        }
        if (!(moveControl instanceof FlyingMoveControl)) {
            moveControl = new FlyingMoveControl(this, 24, true);
        }
        if (!(navigation instanceof FlyingPathNavigation)) {
            FlyingPathNavigation flyingNavigation = new FlyingPathNavigation(this, level());
            flyingNavigation.setCanOpenDoors(false);
            flyingNavigation.setCanFloat(true);
            navigation = flyingNavigation;
        }
        setNoGravity(true);
        if (enderDragonPhaseBoss == null) {
            enderDragonPhaseBoss = new ImportedEnderDragonPhaseBoss(this);
        }
    }

    private void logEnderDragonAnimationTrigger(String source,
                                                String animationName,
                                                boolean loop,
                                                ImportedEntityPacks.EntityAnimationState desiredState) {
        if (!isEnderDragonTemplate()) {
            return;
        }

        boolean sameDesiredState = desiredState == lastLoggedDesiredAnimationState;
        boolean sameAnimationName = animationName.equals(lastLoggedDesiredAnimationName);
        boolean sameAppliedForcedAnimation = (animationName + "|" + loop).equals(lastAppliedForcedAnimation);
        if ("强制动画已应用".equals(source) && sameAppliedForcedAnimation) {
            return;
        }
        if ("常规动画已应用".equals(source) && sameDesiredState && sameAnimationName) {
            return;
        }

        Moreblock.LOGGER.info(
                "末影龙AI动画触发: source={}, entity={}, phaseId={}, desiredState={}, animation={}, loop={}, moving={}, tick={}, pos=({}, {}, {}), speed=({}, {}, {})",
                source,
                getStringUUID(),
                getBossPhaseId(),
                desiredState == null ? "-" : desiredState.name(),
                animationName,
                loop,
                !getDeltaMovement().equals(Vec3.ZERO),
                tickCount,
                round(getX()),
                round(getY()),
                round(getZ()),
                round(getDeltaMovement().x),
                round(getDeltaMovement().y),
                round(getDeltaMovement().z)
        );
        if ("常规动画已应用".equals(source)) {
            lastLoggedDesiredAnimationState = desiredState;
            lastLoggedDesiredAnimationName = animationName;
        }
        if ("强制动画已应用".equals(source)) {
            lastAppliedForcedAnimation = animationName + "|" + loop;
        }
    }

    private static String round(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }
}
