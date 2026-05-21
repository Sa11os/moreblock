package me.sallos.moreblock.item;

import me.sallos.moreblock.client.renderer.ImportedBlockItemRenderer;
import me.sallos.moreblock.config.ImportedBlockPacks;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

@SuppressWarnings("null")
public class ImportedBlockItem extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String definitionKey;

    public ImportedBlockItem(Block block, Properties properties, String definitionKey) {
        super(block, properties);
        this.definitionKey = definitionKey;
    }

    public String getDefinitionKey() {
        return definitionKey;
    }

    @Override
    public Component getName(ItemStack stack) {
        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(definitionKey);
        if (definition == null) {
            return super.getName(stack);
        }
        if (definition.geoSourceFile() == null) {
            return Component.literal(ImportedBlockPacks.resolveDisplayName(definition));
        }
        return Component.translatable(definition.blockTranslationKey());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new ImportedBlockItemRenderer();
                }
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
