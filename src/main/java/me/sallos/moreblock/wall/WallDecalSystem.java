package me.sallos.moreblock.wall;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.network.message.ClearWallDecalsMessage;
import me.sallos.moreblock.network.message.SyncWallDecalMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Moreblock.MODID)
public final class WallDecalSystem {
    private static final Map<Key, DecalPlacement> PLACEMENTS = new ConcurrentHashMap<>();
    private static final Set<ResourceLocation> LOADED_DIMENSIONS = ConcurrentHashMap.newKeySet();

    private WallDecalSystem() {
    }

    public static InteractionResult placeFromItem(UseOnContext context, ResourceLocation texture) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ensureLoaded(level);
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        ResourceLocation dimension = level.dimension().location();
        Key key = new Key(dimension, pos, face);
        DecalPlacement existing = PLACEMENTS.get(key);
        if (existing != null) {
            remove(existing);
            save(level);
            send(level, pos, SyncWallDecalMessage.remove(existing));
            return InteractionResult.CONSUME;
        }

        DecalPlacement placement = new DecalPlacement(dimension, pos, face, texture);
        put(placement);
        save(level);
        send(level, pos, SyncWallDecalMessage.upsert(placement));

        if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    public static void put(DecalPlacement placement) {
        PLACEMENTS.put(new Key(placement.dimension(), placement.pos(), placement.face()), placement);
    }

    public static void remove(DecalPlacement placement) {
        PLACEMENTS.remove(new Key(placement.dimension(), placement.pos(), placement.face()));
    }

    public static Collection<DecalPlacement> getPlacements(ResourceKey<Level> dimension) {
        ResourceLocation dimensionLocation = dimension.location();
        return PLACEMENTS.values().stream()
                .filter(placement -> placement.dimension().equals(dimensionLocation))
                .toList();
    }

    public static void clearDimension(ResourceLocation dimension) {
        PLACEMENTS.entrySet().removeIf(entry -> entry.getKey().dimension().equals(dimension));
        LOADED_DIMENSIONS.remove(dimension);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ensureLoaded(serverPlayer.serverLevel());
        ResourceLocation dimension = serverPlayer.level().dimension().location();
        Moreblock.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClearWallDecalsMessage(dimension));
        for (DecalPlacement placement : getPlacements(serverPlayer.level().dimension())) {
            Moreblock.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), SyncWallDecalMessage.upsert(placement));
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level) {
            removeAllAt(level, event.getPos());
        }
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level) {
            removeAllAt(level, event.getPos());
        }
    }

    @SubscribeEvent
    public static void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
        if (event.getLevel() instanceof Level level) {
            removeAllAt(level, event.getPos());
        }
    }

    private static void removeAllAt(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }

        ensureLoaded(level);
        ResourceLocation dimension = level.dimension().location();
        List<DecalPlacement> removedPlacements = PLACEMENTS.values().stream()
                .filter(placement -> placement.dimension().equals(dimension) && placement.pos().equals(pos))
                .toList();
        if (removedPlacements.isEmpty()) {
            return;
        }
        for (DecalPlacement placement : removedPlacements) {
            remove(placement);
            send(level, pos, SyncWallDecalMessage.remove(placement));
        }
        save(level);
    }

    private static void ensureLoaded(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ResourceLocation dimension = serverLevel.dimension().location();
        if (!LOADED_DIMENSIONS.add(dimension)) {
            return;
        }
        WallDecalSavedData data = getSavedData(serverLevel);
        data.placements().forEach(WallDecalSystem::put);
    }

    private static void save(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        WallDecalSavedData data = getSavedData(serverLevel);
        data.replaceAll(getPlacements(serverLevel.dimension()));
    }

    private static WallDecalSavedData getSavedData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(WallDecalSavedData::load, WallDecalSavedData::new, WallDecalSavedData.dataName());
    }

    private static void send(Level level, BlockPos pos, SyncWallDecalMessage message) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ChunkPos chunkPos = new ChunkPos(pos);
        Moreblock.PACKET_HANDLER.send(
                PacketDistributor.TRACKING_CHUNK.with(() -> serverLevel.getChunk(chunkPos.x, chunkPos.z)),
                message
        );
        serverLevel.players().stream()
                .filter(player -> player instanceof ServerPlayer)
                .map(player -> (ServerPlayer) player)
                .filter(player -> player.blockPosition().closerThan(pos, 64.0d))
                .forEach(player -> Moreblock.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), message));
    }

    private record Key(ResourceLocation dimension, BlockPos pos, Direction face) {
    }

    public record DecalPlacement(ResourceLocation dimension, BlockPos pos, Direction face, ResourceLocation texture) {
    }
}
