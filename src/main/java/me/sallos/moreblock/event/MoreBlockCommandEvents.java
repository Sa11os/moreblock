package me.sallos.moreblock.event;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedBlockPacks;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
                                .executes(context -> checkHeldConfiguredBlock(context.getSource())))));
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
}