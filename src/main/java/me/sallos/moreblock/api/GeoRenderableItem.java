package me.sallos.moreblock.api;

import net.minecraft.resources.ResourceLocation;

/**
 * 通用 geo 物品渲染契约。
 *
 * <p>外部模组（如 emods）自己创建并注册 {@link net.minecraft.world.item.Item}，
 * 只要该物品同时实现 GeckoLib 的 {@code GeoItem} 与本接口，即可复用 MoreBlock 提供的
 * {@link me.sallos.moreblock.client.renderer.GenericGeoItemRenderer} 完成 GeckoLib 渲染。
 *
 * <p>MoreBlock 只负责渲染：读取这三个资源定位并交给 GeckoLib。物品的创建、注册、
 * 属性与玩法逻辑全部由外部模组自行掌控。
 */
public interface GeoRenderableItem {
    /** GeckoLib 模型资源，例如 {@code emods:geo/moreblock/item/m1_helmet/M1.geo.json}。 */
    ResourceLocation geoModel();

    /** 贴图资源，例如 {@code emods:textures/moreblock/item/m1_helmet/1.png}。 */
    ResourceLocation geoTexture();

    /** 动画资源；没有动画时可返回一个空闲动画或任意有效定位。 */
    ResourceLocation geoAnimation();
}
