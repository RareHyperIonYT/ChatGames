package dev.rarehyperion.chatgames.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.rarehyperion.chatgames.platform.PlatformLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class DatabaseManager {

    private final HikariDataSource dataSource;
    private final PlatformLogger logger;

    public DatabaseManager(final String host, final int port, final String database,
                           final String username, final String password,
                           final int poolSize, final PlatformLogger logger) {
        this.logger = logger;

        final HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(5000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(600000);
        config.setPoolName("ChatGames-DB");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "25");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        this.dataSource = new HikariDataSource(config);
    }

    public void createTables() {
        try (final Connection conn = this.dataSource.getConnection();
             final PreparedStatement stmt = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS chatgames_points (" +
                             "uuid VARCHAR(36) PRIMARY KEY, " +
                             "player_name VARCHAR(16) NOT NULL, " +
                             "points INT NOT NULL DEFAULT 0" +
                             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
             )) {
            stmt.executeUpdate();
        } catch (final SQLException e) {
            this.logger.error("Failed to create chatgames_points table: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public void shutdown() {
        if (this.dataSource != null && !this.dataSource.isClosed()) {
            this.dataSource.close();
        }
    }

}
