package dev.rarehyperion.chatgames;

import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.game.GameManager;
import dev.rarehyperion.chatgames.game.GameRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractChatGames extends JavaPlugin {

    protected ConfigManager configManager;
    protected GameRegistry gameRegistry;
    protected GameManager gameManager;

    @Override
    public void onLoad() {
        this.saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        this.getLogger().info("Running on platform: " + this.getPlatformName());

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.gameRegistry = new GameRegistry(this);
        this.gameManager = new GameManager(this, this.configManager, this.gameRegistry);

        // Load configuration and games
        this.configManager.load();
        this.gameRegistry.registerDefaults();
        this.gameRegistry.loadGames();

        // Register commands and listeners
        this.registerCommands();
        this.registerListeners();

        // Start game scheduler
        this.gameManager.startScheduler();

        this.getLogger().info("ChatGames has been enabled!");
    }

    @Override
    public void onDisable() {
        if (this.gameManager != null) {
            this.gameManager.shutdown();
        }

        this.getLogger().info("ChatGames has been disabled!");
    }

    public void reload() {
        this.getLogger().info("Reloading ChatGames...");
        this.reloadConfig();
        this.configManager.load();
        this.gameManager.reload();
        this.getLogger().info("ChatGames reloaded successfully");
    }

    public abstract void sendMessage(final CommandSender sender, final Component component);
    public abstract void broadcast(final Component component);
    public abstract void registerListeners();
    public abstract void registerCommands();
    public abstract String getPlatformName();

    public ConfigManager getConfigManager() { return this.configManager; }
    public GameRegistry getGameRegistry() { return this.gameRegistry; }
    public GameManager getGameManager() { return this.gameManager; }

}
