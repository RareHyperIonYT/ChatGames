package dev.rarehyperion.chatgames.storage;

import dev.rarehyperion.chatgames.platform.PlatformLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class PointsManager {

    private static final String UPSERT_SQL =
            "INSERT INTO chatgames_points (uuid, player_name, points) VALUES (?, ?, 1) " +
                    "ON DUPLICATE KEY UPDATE points = points + 1, player_name = VALUES(player_name)";

    private static final String SELECT_SQL =
            "SELECT points FROM chatgames_points WHERE uuid = ?";

    private static final String SELECT_BY_NAME_SQL =
            "SELECT points FROM chatgames_points WHERE player_name = ? ORDER BY points DESC LIMIT 1";

    private final DatabaseManager db;
    private final PlatformLogger logger;
    private final ExecutorService executor;

    private final ConcurrentHashMap<UUID, AtomicInteger> cache = new ConcurrentHashMap<>();

    public PointsManager(final DatabaseManager db, final PlatformLogger logger) {
        this.db = db;
        this.logger = logger;
        this.executor = Executors.newFixedThreadPool(2, r -> {
            final Thread t = new Thread(r, "ChatGames-Points");
            t.setDaemon(true);
            return t;
        });
    }

    public void addWin(final UUID uuid, final String playerName) {
        this.cache.computeIfAbsent(uuid, k -> new AtomicInteger(0)).incrementAndGet();

        this.executor.execute(() -> {
            try (final Connection conn = this.db.getConnection();
                 final PreparedStatement stmt = conn.prepareStatement(UPSERT_SQL)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, playerName);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                this.logger.error("Failed to save points for " + playerName + ": " + e.getMessage());
            }
        });
    }

    public int getPoints(final UUID uuid) {
        final AtomicInteger cached = this.cache.get(uuid);
        return cached != null ? cached.get() : 0;
    }

    public int getPointsByName(final String playerName) {
        for (final Map.Entry<UUID, AtomicInteger> entry : this.cache.entrySet()) {
            // Fallback: search by name requires DB lookup
        }

        // Not in cache, do sync DB lookup (called from PlaceholderAPI which is already async-safe)
        try (final Connection conn = this.db.getConnection();
             final PreparedStatement stmt = conn.prepareStatement(SELECT_BY_NAME_SQL)) {
            stmt.setString(1, playerName);
            try (final ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("points");
                }
            }
        } catch (final SQLException e) {
            this.logger.error("Failed to fetch points for " + playerName + ": " + e.getMessage());
        }
        return 0;
    }

    public void loadPlayer(final UUID uuid) {
        this.executor.execute(() -> {
            try (final Connection conn = this.db.getConnection();
                 final PreparedStatement stmt = conn.prepareStatement(SELECT_SQL)) {
                stmt.setString(1, uuid.toString());
                try (final ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        this.cache.put(uuid, new AtomicInteger(rs.getInt("points")));
                    } else {
                        this.cache.put(uuid, new AtomicInteger(0));
                    }
                }
            } catch (final SQLException e) {
                this.logger.error("Failed to load points for " + uuid + ": " + e.getMessage());
            }
        });
    }

    public void unloadPlayer(final UUID uuid) {
        this.cache.remove(uuid);
    }

    public void shutdown() {
        this.executor.shutdown();
        try {
            this.executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
