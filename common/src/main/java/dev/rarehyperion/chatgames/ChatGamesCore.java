package dev.rarehyperion.chatgames;

import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.game.GameManager;
import dev.rarehyperion.chatgames.game.GameRegistry;
import dev.rarehyperion.chatgames.platform.Platform;
import dev.rarehyperion.chatgames.storage.DatabaseManager;
import dev.rarehyperion.chatgames.storage.PointsManager;
import dev.rarehyperion.chatgames.versioning.VersionChecker;
import net.kyori.adventure.text.Component;

public final class ChatGamesCore {

    private final Platform platform;

    private ConfigManager configManager;
    private GameRegistry gameRegistry;
    private GameManager gameManager;
    private DatabaseManager databaseManager;
    private PointsManager pointsManager;

    public ChatGamesCore(final Platform platform) {
        this.platform = platform;
    }

    public void load() {
        this.platform.saveDefaultConfig();
    }

    public void enable() {
        this.platform.getLogger().info("Running on platform: " + this.platform.name());

        this.configManager = new ConfigManager(this.platform);
        this.gameRegistry = new GameRegistry(this);
        this.gameManager = new GameManager(this, this.configManager, this.gameRegistry);

        this.configManager.load();
        this.gameRegistry.registerDefaults();
        this.gameRegistry.loadGames();

        this.initDatabase();

        this.gameManager.startScheduler();

        this.platform.registerCommands(this);
        this.platform.registerListeners(this);

        VersionChecker.check(this.platform);

        this.platform.getLogger().info("ChatGames enabled!");

    }

    public void disable() {
        if (this.gameManager != null) this.gameManager.shutdown();
        if (this.pointsManager != null) this.pointsManager.shutdown();
        if (this.databaseManager != null) this.databaseManager.shutdown();
        this.platform.getLogger().info("ChatGames disabled!");
    }

    public void reload() {
        this.platform.getLogger().info("Reloading ChatGames...");
        this.platform.reloadConfig();
        this.configManager.load();
        this.gameManager.reload();
        this.platform.getLogger().info("ChatGames reloaded successfully");
    }

    public void broadcast(final Component component) {
        this.platform.broadcast(component);
    }

    public Platform platform() {
        return this.platform;
    }

    public GameManager gameManager() {
        return this.gameManager;
    }

    public GameRegistry gameRegistry() {
        return this.gameRegistry;
    }

    public ConfigManager configManager() {
        return this.configManager;
    }

    public PointsManager pointsManager() {
        return this.pointsManager;
    }

    public DatabaseManager databaseManager() {
        return this.databaseManager;
    }

    private void initDatabase() {
        final boolean dbEnabled = this.platform.getConfigValue("database.enabled", Boolean.class, false);
        if (!dbEnabled) {
            this.platform.getLogger().info("Database is disabled in config. Points system will not be active.");
            return;
        }

        try {
            final String host = this.platform.getConfigValue("database.host", String.class, "localhost");
            final int port = this.platform.getConfigValue("database.port", Integer.class, 3306);
            final String database = this.platform.getConfigValue("database.database", String.class, "minecraft");
            final String username = this.platform.getConfigValue("database.username", String.class, "root");
            final String password = this.platform.getConfigValue("database.password", String.class, "");
            final int poolSize = this.platform.getConfigValue("database.pool-size", Integer.class, 5);

            this.databaseManager = new DatabaseManager(host, port, database, username, password, poolSize, this.platform.getLogger());
            this.databaseManager.createTables();

            this.pointsManager = new PointsManager(this.databaseManager, this.platform.getLogger());

            this.platform.getLogger().info("Database connected! Points system is active.");
        } catch (final Exception e) {
            this.platform.getLogger().error("Failed to initialize database: " + e.getMessage());
            this.databaseManager = null;
            this.pointsManager = null;
        }
    }

}