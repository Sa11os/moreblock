package me.sallos.moreblock.network;

import me.sallos.moreblock.Moreblock;
import me.sallos.moreblock.config.ImportedBlockPacks;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("null")
public final class ImportedBlockPackSync {
    private static final int VERIFY_TIMEOUT_TICKS = 60;
    private static final Set<UUID> PENDING_PLAYERS = ConcurrentHashMap.newKeySet();
    private static volatile Component rememberedClientDisconnectMessage = null;

    private ImportedBlockPackSync() {
    }

    public static void beginVerification(ServerPlayer player) {
        UUID playerId = player.getUUID();
        MinecraftServer server = player.server;
        PENDING_PLAYERS.add(playerId);

        // 玩家进服后需要先回传客户端的更多方块包清单，超时就拦截，避免带着错误资源继续进服。
        Moreblock.queueServerWork(VERIFY_TIMEOUT_TICKS, () -> {
            ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(playerId);
            if (onlinePlayer == null || !PENDING_PLAYERS.remove(playerId)) {
                return;
            }

            Moreblock.LOGGER.warn("玩家 {} 的更多方块包校验超时，已阻止进入服务器", onlinePlayer.getGameProfile().getName());
            onlinePlayer.connection.disconnect(Component.translatable("disconnect.moreblock.configured_pack.timeout"));
        });
    }

    public static void clearPending(ServerPlayer player) {
        PENDING_PLAYERS.remove(player.getUUID());
    }

    public static void verifyClientManifest(ServerPlayer player, List<ImportedBlockPacks.PackManifestEntry> clientManifest) {
        clearPending(player);

        ManifestComparisonResult result = compareManifests(clientManifest, ImportedBlockPacks.getPackManifest());
        if (result.matches()) {
            Moreblock.LOGGER.info("玩家 {} 的更多方块包校验通过，共 {} 个配置包",
                    player.getGameProfile().getName(),
                    clientManifest.size());
            return;
        }

        Component disconnectMessage = result.buildDisconnectComponent();
        Moreblock.LOGGER.warn("玩家 {} 的更多方块包校验失败：{}\n{}",
                player.getGameProfile().getName(),
                result.summary(),
                result.buildDetails());
        player.connection.disconnect(disconnectMessage);
    }

    public static void verifyClientManifestDuringLogin(Connection connection, List<ImportedBlockPacks.PackManifestEntry> clientManifest) {
        ManifestComparisonResult result = compareManifests(clientManifest, ImportedBlockPacks.getPackManifest());
        if (result.matches()) {
            Moreblock.LOGGER.info("连接 {} 的更多方块包登录校验通过，共 {} 个配置包",
                    describeConnection(connection),
                    clientManifest.size());
            return;
        }

        Component disconnectMessage = result.buildDisconnectComponent();
        Moreblock.LOGGER.warn("连接 {} 的更多方块包登录校验失败：{}\n{}",
                describeConnection(connection),
                result.summary(),
                result.buildDetails());
        disconnectDuringLogin(connection, disconnectMessage);
    }

    public static ManifestComparisonResult compareManifests(List<ImportedBlockPacks.PackManifestEntry> clientManifest,
                                                            List<ImportedBlockPacks.PackManifestEntry> serverManifest) {
        Map<String, ImportedBlockPacks.PackManifestEntry> clientByRegistry = indexByRegistryName(clientManifest);
        Map<String, ImportedBlockPacks.PackManifestEntry> serverByRegistry = indexByRegistryName(serverManifest);
        List<String> missingOnClient = new ArrayList<>();
        List<String> extraOnClient = new ArrayList<>();
        List<String> differentContent = new ArrayList<>();

        for (Map.Entry<String, ImportedBlockPacks.PackManifestEntry> entry : serverByRegistry.entrySet()) {
            ImportedBlockPacks.PackManifestEntry clientEntry = clientByRegistry.get(entry.getKey());
            if (clientEntry == null) {
                missingOnClient.add(entry.getValue().describe());
                continue;
            }

            if (!entry.getValue().fingerprint().equals(clientEntry.fingerprint())) {
                differentContent.add(entry.getValue().describe());
            }
        }

        for (Map.Entry<String, ImportedBlockPacks.PackManifestEntry> entry : clientByRegistry.entrySet()) {
            if (!serverByRegistry.containsKey(entry.getKey())) {
                extraOnClient.add(entry.getValue().describe());
            }
        }

        missingOnClient.sort(Comparator.naturalOrder());
        extraOnClient.sort(Comparator.naturalOrder());
        differentContent.sort(Comparator.naturalOrder());
        return new ManifestComparisonResult(missingOnClient, extraOnClient, differentContent);
    }

    public static void rememberClientDisconnectMessage(Component message) {
        rememberedClientDisconnectMessage = message;
    }

    public static Component consumeRememberedClientDisconnectMessage() {
        Component message = rememberedClientDisconnectMessage;
        rememberedClientDisconnectMessage = null;
        return message;
    }

    private static void disconnectDuringLogin(Connection connection, Component reason) {
        PacketListener listener = connection.getPacketListener();
        if (listener instanceof ServerLoginPacketListenerImpl loginListener) {
            loginListener.disconnect(reason);
            return;
        }

        connection.disconnect(reason);
    }

    private static String describeConnection(Connection connection) {
        PacketListener listener = connection.getPacketListener();
        if (listener instanceof ServerLoginPacketListenerImpl loginListener) {
            return loginListener.getUserName();
        }

        return String.valueOf(connection.getRemoteAddress());
    }

    private static Map<String, ImportedBlockPacks.PackManifestEntry> indexByRegistryName(List<ImportedBlockPacks.PackManifestEntry> manifest) {
        Map<String, ImportedBlockPacks.PackManifestEntry> indexed = new LinkedHashMap<>();
        for (ImportedBlockPacks.PackManifestEntry entry : manifest) {
            indexed.put(entry.registryName(), entry);
        }
        return indexed;
    }

    public record ManifestComparisonResult(
            List<String> missingOnClient,
            List<String> extraOnClient,
            List<String> differentContent
    ) {
        public boolean matches() {
            return missingOnClient.isEmpty() && extraOnClient.isEmpty() && differentContent.isEmpty();
        }

        public String summary() {
            return "客户端缺少 " + missingOnClient.size() + " 个，客户端多出 " + extraOnClient.size()
                    + " 个，内容不一致 " + differentContent.size() + " 个";
        }

        public Component buildDisconnectComponent() {
            MutableComponent builder = Component.empty()
                    .append(Component.translatable("disconnect.moreblock.configured_pack.mismatch.header"))
                    .append("\n\n");
            appendTranslatedGroup(builder, "disconnect.moreblock.configured_pack.mismatch.missing", missingOnClient);
            appendTranslatedGroup(builder, "disconnect.moreblock.configured_pack.mismatch.extra", extraOnClient);
            appendTranslatedGroup(builder, "disconnect.moreblock.configured_pack.mismatch.different", differentContent);
            builder.append(Component.translatable("disconnect.moreblock.configured_pack.mismatch.footer"));
            return builder;
        }

        public String buildDetails() {
            StringBuilder builder = new StringBuilder();
            appendGroup(builder, "客户端缺少以下包：", missingOnClient);
            appendGroup(builder, "客户端多出以下包：", extraOnClient);
            appendGroup(builder, "以下包内容与服务端不一致：", differentContent);
            if (builder.length() == 0) {
                return "无差异明细";
            }
            return builder.toString().strip();
        }

        private static void appendGroup(StringBuilder builder, String title, List<String> entries) {
            if (entries.isEmpty()) {
                return;
            }

            builder.append(title).append('\n');
            for (String entry : entries) {
                builder.append("- ").append(entry).append('\n');
            }
        }

        private static void appendTranslatedGroup(MutableComponent builder, String titleKey, List<String> entries) {
            if (entries.isEmpty()) {
                return;
            }

            builder.append(Component.translatable(titleKey)).append("\n");
            for (String entry : entries) {
                builder.append(Component.literal("- " + entry)).append("\n");
            }
        }
    }
}
