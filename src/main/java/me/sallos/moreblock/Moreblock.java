package me.sallos.moreblock;

import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.init.ImportedBlockEntities;
import me.sallos.moreblock.init.ImportedBlocks;
import me.sallos.moreblock.init.MoreBlockCreativeTabs;
import me.sallos.moreblock.init.MoreBlockEntityTypes;
import me.sallos.moreblock.init.ImportedItems;
import me.sallos.moreblock.network.MoreBlockNetworkMessages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.GeckoLib;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod(Moreblock.MODID)
@SuppressWarnings("null")
public class Moreblock {
    public static final Logger LOGGER = LogManager.getLogger(Moreblock.class);
    public static final String MODID = "moreblock";

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel LOGIN_PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MODID, "imported_block_pack_sync"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MODID, MODID),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> WORK_QUEUE = new ConcurrentLinkedQueue<>();
    private static int messageId = 0;

    public Moreblock(FMLJavaModLoadingContext context) {
        ImportedBlockPacks.bootstrap();
        GeckoLib.initialize();
        MinecraftForge.EVENT_BUS.register(this);

        IEventBus modEventBus = context.getModEventBus();
        ImportedBlocks.register(modEventBus);
        ImportedBlockEntities.register(modEventBus);
        MoreBlockEntityTypes.register(modEventBus);
        ImportedItems.register(modEventBus);
        MoreBlockCreativeTabs.register(modEventBus);
        MoreBlockNetworkMessages.register();
    }

    public static <T> void addNetworkMessage(Class<T> messageType,
                                             BiConsumer<T, FriendlyByteBuf> encoder,
                                             Function<FriendlyByteBuf, T> decoder,
                                             BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        PACKET_HANDLER.registerMessage(messageId, messageType, encoder, decoder, messageConsumer);
        messageId++;
    }

    public static void queueServerWork(int tick, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
            WORK_QUEUE.add(new AbstractMap.SimpleEntry<>(action, tick));
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
        WORK_QUEUE.forEach(work -> {
            work.setValue(work.getValue() - 1);
            if (work.getValue() == 0) {
                actions.add(work);
            }
        });
        actions.forEach(entry -> entry.getKey().run());
        WORK_QUEUE.removeAll(actions);
    }
}
