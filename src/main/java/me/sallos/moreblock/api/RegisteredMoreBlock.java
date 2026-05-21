package me.sallos.moreblock.api;

import me.sallos.moreblock.config.ImportedBlockPacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

public final class RegisteredMoreBlock {
    private final String registryName;

    public RegisteredMoreBlock(String registryName) {
        this.registryName = registryName;
    }

    public String registryName() {
        return registryName;
    }

    public ResourceLocation blockId() {
        return ResourceLocation.fromNamespaceAndPath("moreblock", registryName);
    }

    public Optional<RegistryObject<Block>> block() {
        return ImportedBlockPacks.getDynamicBlockRegistryObject(registryName);
    }

    public Optional<RegistryObject<Item>> item() {
        return ImportedBlockPacks.getDynamicItemRegistryObject(registryName);
    }
}
