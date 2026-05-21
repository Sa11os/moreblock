package me.sallos.moreblock.api;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record MoreBlockBlockDefinition(
        String ownerModId,
        String id,
        String zhCnName,
        String enUsName,
        ResourceLocation geo,
        ResourceLocation texture,
        ResourceLocation display,
        String hitboxBoneName,
        boolean showInMoreBlockTab,
        boolean translucent,
        int lightLevel,
        boolean supportsSitting,
        double seatHeight,
        boolean supportsLying,
        double lyingHeight,
        int lyingRotationCompensation
) {
    public MoreBlockBlockDefinition {
        Objects.requireNonNull(ownerModId, "ownerModId");
        Objects.requireNonNull(id, "id");
    }

    public String registryName() {
        return ownerModId + '_' + id;
    }

    public String displayName() {
        if (zhCnName != null && !zhCnName.isBlank()) {
            return zhCnName;
        }
        if (enUsName != null && !enUsName.isBlank()) {
            return enUsName;
        }
        return id;
    }
}
