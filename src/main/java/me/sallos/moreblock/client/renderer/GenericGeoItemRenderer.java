package me.sallos.moreblock.client.renderer;

import me.sallos.moreblock.api.GeoRenderableItem;
import me.sallos.moreblock.client.model.GenericGeoItemModel;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * 通用 geo 物品渲染器，供外部模组在 {@code IClientItemExtensions#getCustomRenderer()} 中复用。
 *
 * <p>用法（外部模组物品类的 {@code initializeClient}）：
 * <pre>{@code
 * consumer.accept(new IClientItemExtensions() {
 *     private BlockEntityWithoutLevelRenderer renderer;
 *     @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
 *         if (renderer == null) renderer = new GenericGeoItemRenderer<>();
 *         return renderer;
 *     }
 * });
 * }</pre>
 */
public class GenericGeoItemRenderer<T extends Item & GeoAnimatable & GeoRenderableItem> extends GeoItemRenderer<T> {
    public GenericGeoItemRenderer() {
        super(new GenericGeoItemModel<>());
    }
}
