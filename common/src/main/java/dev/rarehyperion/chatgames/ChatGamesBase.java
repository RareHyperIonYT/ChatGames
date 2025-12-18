package dev.rarehyperion.chatgames;

import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.game.GameManager;
import dev.rarehyperion.chatgames.game.GameRegistry;
import dev.rarehyperion.chatgames.platform.ChatGamesPlugin;

public abstract class ChatGamesBase {

    protected final ChatGamesPlugin plugin;
    protected ConfigManager configManager;
    protected GameRegistry gameRegistry;
    protected GameManager gameManager;

    protected ChatGamesBase(ChatGamesPlugin plugin) {
        this.plugin = plugin;
    }

    public void onPluginLoad() {
        this.plugin.saveDefaultConfig();
    }

    public void onPluginEnable() {
        this.plugin.getLogger().info("Running on platform: " + this.plugin.getPlatformName());

        // Initialize managers
        this.configManager = new ConfigManager(this.plugin);
        this.gameRegistry = new GameRegistry(this.plugin);
        this.gameManager = new GameManager(this.plugin, this.configManager, this.gameRegistry);

        // Store references in plugin
        this.plugin.setConfigManager(configManager);
        this.plugin.setGameRegistry(gameRegistry);
        this.plugin.setGameManager(gameManager);

        // Load configuration and games
        this.configManager.load();
        this.gameRegistry.registerDefaults();
        this.gameRegistry.loadGames();

        // Register commands and listeners
        this.plugin.registerCommands();
        this.plugin.registerListeners();

        // Start game scheduler
        this.gameManager.startScheduler();

        this.plugin.getLogger().info("ChatGames has been enabled!");
    }

    public void onPluginDisable() {
        if (this.gameManager != null) {
            this.gameManager.shutdown();
        }

        this.plugin.getLogger().info("ChatGames has been disabled!");
    }

    public void reload() {
        this.plugin.getLogger().info("Reloading ChatGames...");
        this.plugin.reloadConfig();
        this.configManager.load();
        this.gameManager.reload();
        this.plugin.getLogger().info("ChatGames reloaded successfully");
    }

}
