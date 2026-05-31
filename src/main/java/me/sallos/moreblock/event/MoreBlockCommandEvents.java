package me.sallos.moreblock.event;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.config.ImportedEntityPacks;
import me.sallos.moreblock.entity.SeatEntity;
import me.sallos.moreblock.network.message.OpenSeatHeightDebugScreenMessage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collection;

@Mod.EventBusSubscriber(modid = Moreblock.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@SuppressWarnings("null")
public final class MoreBlockCommandEvents {
    private MoreBlockCommandEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("moreblock")
                .then(Commands.literal("block")
                        .then(Commands.literal("list")
                                .executes(context -> listLoadedConfiguredBlocks(context.getSource())))
                        .then(Commands.literal("check")
                                .executes(context -> checkHeldConfiguredBlock(context.getSource())))
                        .then(Commands.literal("seat_debug")
                                .executes(context -> openSeatHeightDebugScreen(context.getSource()))))
                .then(Commands.literal("entity")
                        .then(Commands.literal("list")
                                .executes(context -> listLoadedConfiguredEntities(context.getSource())))
                        .then(Commands.literal("check")
                                .executes(context -> checkHeldConfiguredEntity(context.getSource())))));
    }

    private static int checkHeldConfiguredBlock(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            source.sendFailure(Component.literal("你手上没有持有任何物品。"));
            return 0;
        }

        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(held.getItem());
        if (definition == null) {
            source.sendFailure(Component.literal("手持物品不是导入的方块。"));
            return 0;
        }

        String zipName = definition.sourceZipName();
        if (zipName != null) {
            source.sendSuccess(() -> Component.literal(
                    "方块「" + definition.displayName() + "」[" + definition.registryName() + "] 来自压缩包：" + zipName), false);
        } else {
            source.sendSuccess(() -> Component.literal(
                    "方块「" + definition.displayName() + "」[" + definition.registryName() + "] 来自文件夹：" + definition.sourceFolderName()), false);
        }
        return 1;
    }

    private static int checkHeldConfiguredBlockSeatHeight(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ImportedBlockPacks.Definition definition = getHeldConfiguredBlockDefinition(player, source);
        if (definition == null) {
            return 0;
        }
        if (!definition.supportsSitting()) {
            source.sendFailure(Component.literal("手持导入方块不支持坐下。"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(buildSeatHeightDebugText(definition)), false);
        return 1;
    }

    private static int listLoadedConfiguredBlocks(CommandSourceStack source) {
        Collection<ImportedBlockPacks.Definition> definitions = ImportedBlockPacks.getDefinitions();
        if (definitions.isEmpty()) {
            source.sendSuccess(() -> Component.literal("当前没有读取到任何导入方块。"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("当前已读取 " + definitions.size() + " 个导入方块："), false);
        for (ImportedBlockPacks.Definition definition : definitions) {
            source.sendSuccess(() -> Component.literal("- " + definition.displayName() + " [" + definition.registryName() + "]"), false);
        }
        return definitions.size();
    }

    private static int openSeatHeightDebugScreen(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!(player.getVehicle() instanceof SeatEntity seatEntity) || !seatEntity.hasPassenger(player)) {
            source.sendFailure(Component.literal("请先坐在 MoreBlock 座椅上，再打开坐高调试界面。"));
            return 0;
        }

        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(
                player.level().getBlockState(seatEntity.getSeatBlockPos()).getBlock()
        );
        String displayName = definition == null ? "当前座椅" : definition.displayName();
        String registryName = definition == null ? "unknown" : definition.registryName();

        Moreblock.PACKET_HANDLER.send(
                PacketDistributor.PLAYER.with(() -> player),
                new OpenSeatHeightDebugScreenMessage(displayName, registryName, seatEntity.getConfiguredSeatHeight())
        );
        source.sendSuccess(() -> Component.literal("已打开坐高调试界面，拖动滑条会实时应用到当前座椅。"), false);
        return 1;
    }

    private static int checkHeldConfiguredEntity(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            source.sendFailure(Component.literal("你手上没有持有任何物品。"));
            return 0;
        }

        ImportedEntityPacks.Definition definition = ImportedEntityPacks.getDefinition(held.getItem());
        if (definition == null) {
            source.sendFailure(Component.literal("手持物品不是导入实体的刷怪蛋。"));
            return 0;
        }

        String zipName = definition.sourceZipName();
        if (zipName != null) {
            source.sendSuccess(() -> Component.literal(
                    "实体「" + definition.displayName() + "」[" + definition.registryName() + "] 来自压缩包：" + zipName), false);
        } else {
            source.sendSuccess(() -> Component.literal(
                    "实体「" + definition.displayName() + "」[" + definition.registryName() + "] 来自文件夹：" + definition.sourceFolderName()), false);
        }
        return 1;
    }

    private static int listLoadedConfiguredEntities(CommandSourceStack source) {
        Collection<ImportedEntityPacks.Definition> definitions = ImportedEntityPacks.getDefinitions();
        if (definitions.isEmpty()) {
            source.sendSuccess(() -> Component.literal("当前没有读取到任何导入实体。"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("当前已读取 " + definitions.size() + " 个导入实体："), false);
        for (ImportedEntityPacks.Definition definition : definitions) {
            source.sendSuccess(() -> Component.literal("- " + definition.displayName() + " [" + definition.registryName() + "]"), false);
        }
        return definitions.size();
    }

    private static ImportedBlockPacks.Definition getHeldConfiguredBlockDefinition(ServerPlayer player, CommandSourceStack source) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            source.sendFailure(Component.literal("你手上没有持有任何物品。"));
            return null;
        }

        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(held.getItem());
        if (definition == null) {
            source.sendFailure(Component.literal("手持物品不是导入的方块。"));
            return null;
        }
        return definition;
    }

    private static String buildSeatHeightDebugText(ImportedBlockPacks.Definition definition) {
        double seatHeight = definition.seatHeight();
        double seatPixels = seatHeight * 16.0d;
        return "方块「" + definition.displayName()
                + "」[" + definition.registryName() + "] 坐高="
                + formatSeatHeight(seatHeight) + " (" + formatSeatHeight(seatPixels) + "px)";
    }

    private static String formatSeatHeight(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }
}
