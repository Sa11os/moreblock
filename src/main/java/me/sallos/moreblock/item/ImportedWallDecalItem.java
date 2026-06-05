package me.sallos.moreblock.item;

import me.sallos.moreblock.config.ImportedWallDecals;
import me.sallos.moreblock.wall.WallDecalSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ImportedWallDecalItem extends Item {
    private final String definitionKey;

    public ImportedWallDecalItem(Properties properties, String definitionKey) {
        super(properties);
        this.definitionKey = definitionKey;
    }

    @Override
    public Component getName(ItemStack stack) {
        ImportedWallDecals.Definition definition = ImportedWallDecals.getDefinition(definitionKey);
        if (definition == null) {
            return super.getName(stack);
        }
        return Component.translatable(definition.itemTranslationKey());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ImportedWallDecals.Definition definition = ImportedWallDecals.getDefinition(definitionKey);
        if (definition == null) {
            return InteractionResult.PASS;
        }
        return WallDecalSystem.placeFromItem(context, definition.textureLocation());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltipComponents, flag);
        ImportedWallDecals.Definition definition = ImportedWallDecals.getDefinition(definitionKey);
        if (definition == null || !definition.hasLore()) {
            return;
        }
        for (int lineNumber = 1; lineNumber <= definition.loreLineCount(); lineNumber++) {
            tooltipComponents.add(Component.translatable(definition.itemLoreTranslationKey(lineNumber)).withStyle(ChatFormatting.GRAY));
        }
    }
}
