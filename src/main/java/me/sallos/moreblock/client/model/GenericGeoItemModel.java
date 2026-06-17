package me.sallos.moreblock.client.model;

import me.sallos.moreblock.api.GeoRenderableItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

/**
 * 通用 geo 物品模型：直接从 {@link GeoRenderableItem} 读取模型/贴图/动画资源。
 *
 * <p>泛型上界要求同时是 {@link GeoAnimatable}（GeckoLib 渲染所需）与 {@link GeoRenderableItem}
 * （提供资源定位），因此可服务于任意实现这两者的外部模组物品。
 */
public class GenericGeoItemModel<T extends Item & GeoAnimatable & GeoRenderableItem> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(T animatable) {
        return animatable.geoModel();
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return animatable.geoTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return animatable.geoAnimation();
    }
}
