package me.sallos.moreblock.entity;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedEntityPacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

final class ImportedEntityAiTemplates {
    private ImportedEntityAiTemplates() {
    }

    static boolean apply(ImportedEntity entity, ImportedEntityPacks.Definition definition) {
        ResourceLocation aiTemplate = definition.aiTemplate();
        if (aiTemplate == null) {
            return false;
        }

        return switch (aiTemplate.toString()) {
            case "minecraft:cow", "minecraft:pig", "minecraft:sheep", "minecraft:chicken", "minecraft:mooshroom" ->
                    applyPassiveAnimalTemplate(entity);
            case "minecraft:zombie", "minecraft:husk" -> applyZombieTemplate(entity);
            case "minecraft:spider", "minecraft:cave_spider" -> applySpiderTemplate(entity);
            case "minecraft:skeleton", "minecraft:stray" -> applySkeletonTemplate(entity);
            case "minecraft:ender_dragon" -> applyEnderDragonTemplate(entity);
            default -> {
                Moreblock.LOGGER.warn("导入实体 {} 使用了暂未适配的 AI 模板 {}，已回退为空闲游荡逻辑",
                        definition.registryName(),
                        aiTemplate);
                yield false;
            }
        };
    }

    private static boolean applyPassiveAnimalTemplate(ImportedEntity entity) {
        entity.addGoal(1, new net.minecraft.world.entity.ai.goal.PanicGoal(entity, 1.25d));
        entity.addGoal(3, new AvoidEntityGoal<>(entity, Player.class, 6.0f, 1.0d, 1.2d));
        entity.addGoal(6, new WaterAvoidingRandomStrollGoal(entity, 1.0d));
        entity.addGoal(7, new LookAtPlayerGoal(entity, Player.class, 6.0f));
        entity.addGoal(8, new RandomLookAroundGoal(entity));
        return true;
    }

    private static boolean applyZombieTemplate(ImportedEntity entity) {
        entity.addGoal(2, new MeleeAttackGoal(entity, 1.0d, false));
        entity.addGoal(5, new WaterAvoidingRandomStrollGoal(entity, 1.0d));
        entity.addGoal(6, new FleeSunGoal(entity, 1.0d));
        entity.addGoal(7, new LookAtPlayerGoal(entity, Player.class, 8.0f));
        entity.addGoal(8, new RandomLookAroundGoal(entity));
        entity.addTargetGoal(1, new HurtByTargetGoal(entity));
        entity.addTargetGoal(2, new NearestAttackableTargetGoal<>(entity, Player.class, true));
        return true;
    }

    private static boolean applySpiderTemplate(ImportedEntity entity) {
        entity.addGoal(2, new LeapAtTargetGoal(entity, 0.4f));
        entity.addGoal(3, new MeleeAttackGoal(entity, 1.1d, true));
        entity.addGoal(6, new WaterAvoidingRandomStrollGoal(entity, 0.9d));
        entity.addGoal(7, new LookAtPlayerGoal(entity, Player.class, 8.0f));
        entity.addGoal(8, new RandomLookAroundGoal(entity));
        entity.addTargetGoal(1, new HurtByTargetGoal(entity));
        entity.addTargetGoal(2, new NearestAttackableTargetGoal<>(entity, Player.class, true));
        return true;
    }

    private static boolean applySkeletonTemplate(ImportedEntity entity) {
        entity.addGoal(2, new RangedAttackGoal(entity, 1.0d, 24, 15.0f));
        entity.addGoal(5, new WaterAvoidingRandomStrollGoal(entity, 1.0d));
        entity.addGoal(6, new FleeSunGoal(entity, 1.0d));
        entity.addGoal(7, new LookAtPlayerGoal(entity, Player.class, 12.0f));
        entity.addGoal(8, new RandomLookAroundGoal(entity));
        entity.addTargetGoal(1, new HurtByTargetGoal(entity));
        entity.addTargetGoal(2, new NearestAttackableTargetGoal<>(entity, Player.class, true));
        return true;
    }

    private static boolean applyEnderDragonTemplate(ImportedEntity entity) {
        entity.addTargetGoal(1, new HurtByTargetGoal(entity));
        entity.addTargetGoal(2, new NearestAttackableTargetGoal<>(entity, Player.class, true));
        return true;
    }
}
