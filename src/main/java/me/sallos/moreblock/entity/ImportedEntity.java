package me.sallos.moreblock.entity;

import me.sallos.moreblock.config.ImportedEntityPacks;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

@SuppressWarnings("null")
public class ImportedEntity extends PathfinderMob implements GeoEntity, RangedAttackMob {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ImportedEntity(EntityType<? extends ImportedEntity> entityType, Level level) {
        super(entityType, level);
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
                0,
                0,
                true,
                true,
                null,
                null,
                null)
                : definition;
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, resolvedDefinition.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, resolvedDefinition.movementSpeed())
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
