package me.sallos.moreblock.init;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.entity.SeatEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MoreBlockEntityTypes {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Moreblock.MODID);

    public static final RegistryObject<EntityType<SeatEntity>> SEAT = REGISTRY.register("seat", () -> EntityType.Builder
            .<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
            .sized(0.0f, 0.0f)
            .clientTrackingRange(8)
            .updateInterval(20)
            .build("seat"));

    private MoreBlockEntityTypes() {
    }

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
