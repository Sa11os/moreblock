package me.sallos.moreblock;

import me.sallos.moreblock.config.ImportedBlockPacks;
import me.sallos.moreblock.config.ImportedBlockPackDownloads;
import me.sallos.moreblock.config.ImportedEntityPacks;
import me.sallos.moreblock.init.ImportedBlockEntities;
import me.sallos.moreblock.init.ImportedBlocks;
import me.sallos.moreblock.init.ImportedEntityItems;
import me.sallos.moreblock.init.ImportedEntityTypes;
import me.sallos.moreblock.init.MoreBlockCreativeTabs;
import me.sallos.moreblock.init.MoreBlockEntityTypes;
import me.sallos.moreblock.init.ImportedItems;
import me.sallos.moreblock.network.MoreBlockNetworkMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.Event;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final Set<UUID> DAYTIME_LYING_PLAYERS = ConcurrentHashMap.newKeySet();
    private static int messageId = 0;

    public Moreblock(FMLJavaModLoadingContext context) {
        ImportedBlockPacks.bootstrap();
        ImportedBlockPackDownloads.bootstrap();
        ImportedEntityPacks.bootstrap();
        GeckoLib.initialize();
        MinecraftForge.EVENT_BUS.register(this);

        IEventBus modEventBus = context.getModEventBus();
        ImportedBlocks.register(modEventBus);
        ImportedBlockEntities.register(modEventBus);
        ImportedEntityTypes.register(modEventBus);
        MoreBlockEntityTypes.register(modEventBus);
        ImportedItems.register(modEventBus);
        ImportedEntityItems.register(modEventBus);
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

    public static void beginDaytimeLying(Player player, BlockPos sleepingPos, ImportedBlockPacks.Definition definition) {
        DAYTIME_LYING_PLAYERS.add(player.getUUID());
        player.startSleeping(sleepingPos);
        applyDaytimeLyingPose(player, sleepingPos, definition);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.serverLevel().updateSleepingPlayerList();
        }
        LOGGER.info("导入方块床白天躺下已开始: block={}, pos={}, player={}, height={}, compensation={}",
                definition.registryName(),
                sleepingPos,
                player.getGameProfile().getName(),
                definition.lyingHeight(),
                definition.lyingRotationCompensation());
    }

    public static void clearDaytimeLying(Player player) {
        DAYTIME_LYING_PLAYERS.remove(player.getUUID());
    }

    private static boolean isDaytimeLying(Player player) {
        return DAYTIME_LYING_PLAYERS.contains(player.getUUID());
    }

    private static void applyDaytimeLyingPose(Player player, BlockPos sleepingPos, ImportedBlockPacks.Definition definition) {
        player.setPose(Pose.SLEEPING);
        player.setDeltaMovement(Vec3.ZERO);
        player.setPos(sleepingPos.getX() + 0.5d, sleepingPos.getY() + definition.lyingHeight(), sleepingPos.getZ() + 0.5d);
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

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        Player player = event.player;
        if (!isDaytimeLying(player)) {
            return;
        }

        Optional<BlockPos> sleepingPos = player.getSleepingPos();
        if (!player.isSleeping() || sleepingPos.isEmpty()) {
            clearDaytimeLying(player);
            return;
        }

        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(player.level().getBlockState(sleepingPos.get()).getBlock());
        if (definition == null || !definition.supportsLying()) {
            clearDaytimeLying(player);
            return;
        }

        applyDaytimeLyingPose(player, sleepingPos.get(), definition);
    }

    @SubscribeEvent
    public void onSleepingTimeCheck(SleepingTimeCheckEvent event) {
        Player player = event.getEntity();
        if (!isDaytimeLying(player) || event.getSleepingLocation().isEmpty()) {
            return;
        }

        ImportedBlockPacks.Definition definition = ImportedBlockPacks.getDefinition(player.level().getBlockState(event.getSleepingLocation().get()).getBlock());
        if (definition == null || !definition.supportsLying()) {
            clearDaytimeLying(player);
            return;
        }

        event.setResult(Event.Result.ALLOW);
    }

    @SubscribeEvent
    public void onPlayerWakeUp(PlayerWakeUpEvent event) {
        clearDaytimeLying(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        clearDaytimeLying(event.getEntity());
    }
}
