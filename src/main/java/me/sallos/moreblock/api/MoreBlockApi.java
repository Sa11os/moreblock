package me.sallos.moreblock.api;

import me.sallos.moreblock.config.ImportedBlockPacks;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class MoreBlockApi {
    private MoreBlockApi() {
    }

    public static RegisteredMoreBlock registerBlock(MoreBlockBlockDefinition definition) {
        return ImportedBlockPacks.registerApiBlock(Objects.requireNonNull(definition, "definition"));
    }

    public static Builder builder(String ownerModId, String id) {
        return new Builder(ownerModId, id);
    }

    public static final class Builder {
        private final String ownerModId;
        private final String id;
        private String zhCnName;
        private String enUsName;
        private ResourceLocation geo;
        private ResourceLocation texture;
        private ResourceLocation display;
        private String hitboxBoneName;
        private boolean showInMoreBlockTab = true;
        private boolean translucent = true;
        private int lightLevel;
        private boolean supportsSitting;
        private double seatHeight = 0.35d;
        private boolean supportsLying;
        private double lyingHeight = 0.2d;
        private int lyingRotationCompensation = 1;

        private Builder(String ownerModId, String id) {
            this.ownerModId = ownerModId;
            this.id = id;
        }

        public Builder name(String zhCnName, String enUsName) {
            this.zhCnName = zhCnName;
            this.enUsName = enUsName;
            return this;
        }

        public Builder resourceBase(String path) {
            String texturePath = path.startsWith("geo/") ? path.substring("geo/".length()) : path;
            this.geo = ResourceLocation.fromNamespaceAndPath(ownerModId, path + ".geo.json");
            this.texture = ResourceLocation.fromNamespaceAndPath(ownerModId, "textures/" + texturePath + ".png");
            this.display = ResourceLocation.fromNamespaceAndPath(ownerModId, "models/item/" + id + ".json");
            return this;
        }

        public Builder geo(ResourceLocation geo) {
            this.geo = geo;
            return this;
        }

        public Builder texture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public Builder display(ResourceLocation display) {
            this.display = display;
            return this;
        }

        public Builder hitboxBoneName(String hitboxBoneName) {
            this.hitboxBoneName = hitboxBoneName;
            return this;
        }

        public Builder showInMoreBlockTab(boolean showInMoreBlockTab) {
            this.showInMoreBlockTab = showInMoreBlockTab;
            return this;
        }

        public Builder translucent(boolean translucent) {
            this.translucent = translucent;
            return this;
        }

        public Builder lightLevel(int lightLevel) {
            this.lightLevel = lightLevel;
            return this;
        }

        public Builder sitting(double seatHeight) {
            this.supportsSitting = true;
            this.seatHeight = seatHeight;
            return this;
        }

        public Builder lying(double lyingHeight, int lyingRotationCompensation) {
            this.supportsLying = true;
            this.lyingHeight = lyingHeight;
            this.lyingRotationCompensation = lyingRotationCompensation;
            return this;
        }

        public RegisteredMoreBlock register() {
            return MoreBlockApi.registerBlock(build());
        }

        public MoreBlockBlockDefinition build() {
            return new MoreBlockBlockDefinition(
                    ownerModId,
                    id,
                    zhCnName,
                    enUsName,
                    geo,
                    texture,
                    display,
                    hitboxBoneName,
                    showInMoreBlockTab,
                    translucent,
                    lightLevel,
                    supportsSitting,
                    seatHeight,
                    supportsLying,
                    lyingHeight,
                    lyingRotationCompensation
            );
        }
    }
}
