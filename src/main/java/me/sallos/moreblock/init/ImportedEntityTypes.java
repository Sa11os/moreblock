package me.sallos.moreblock.init;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedEntityPacks;
import me.sallos.moreblock.entity.ImportedEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public final class ImportedEntityTypes {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Moreblock.MODID);

    static {
        ImportedEntityPacks.registerEntityTypes(REGISTRY);
    }

    private ImportedEntityTypes() {
    }

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

    @Mod.EventBusSubscriber(modid = Moreblock.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            for (Map.Entry<String, RegistryObject<EntityType<ImportedEntity>>> entry : ImportedEntityPacks.getDynamicEntityTypeRegistryObjects().entrySet()) {
                if (entry.getValue().isPresent()) {
                    ImportedEntityPacks.Definition definition = ImportedEntityPacks.getDefinition(entry.getKey());
                    event.put(entry.getValue().get(), ImportedEntity.createAttributes(definition).build());
                }
            }
        }
    }
}
