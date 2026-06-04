package me.sallos.moreblock.workbench;

import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.config.ImportedWallDecals;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

public final class MoreBlockWorkbenchCrafting {
    public static final String TYPE_BLOCK = "block";
    public static final String TYPE_WALL_DECAL = "wall_decal";

    private MoreBlockWorkbenchCrafting() {
    }

    public static void craft(ServerPlayer player, String type, String registryName) {
        CraftTarget target = resolveTarget(type, registryName);
        if (target == null || !consume(player, target.ingredient())) {
            return;
        }

        ItemStack result = new ItemStack(target.result());
        if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }
    }

    private static CraftTarget resolveTarget(String type, String registryName) {
        if (TYPE_BLOCK.equals(type)) {
            Optional<RegistryObject<Item>> item = ImportedBlockPacks.getDynamicItemRegistryObject(registryName);
            if (item.isPresent() && item.get().isPresent()) {
                return new CraftTarget(item.get().get(), Items.IRON_INGOT);
            }
            return null;
        }
        if (TYPE_WALL_DECAL.equals(type)) {
            Optional<RegistryObject<Item>> item = ImportedWallDecals.getDynamicItemRegistryObject(registryName);
            if (item.isPresent() && item.get().isPresent()) {
                return new CraftTarget(item.get().get(), Items.PAPER);
            }
        }
        return null;
    }

    private static boolean consume(ServerPlayer player, Item ingredient) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
            ItemStack stack = player.getInventory().getItem(index);
            if (stack.is(ingredient)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private record CraftTarget(Item result, Item ingredient) {
    }
}
