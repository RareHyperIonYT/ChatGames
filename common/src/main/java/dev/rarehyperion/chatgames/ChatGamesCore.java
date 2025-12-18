package dev.rarehyperion.chatgames;

import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.game.GameManager;
import dev.rarehyperion.chatgames.game.GameRegistry;
import dev.rarehyperion.chatgames.platform.Platform;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public final class ChatGamesCore {

    private final Platform platform;

    private ConfigManager configManager;
    private GameRegistry gameRegistry;
    private GameManager gameManager;

    public ChatGamesCore(final Platform platform) {
        this.platform = platform;
    }

    public void load() {
        this.platform.onLoad();
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

        this.gameManager.startScheduler();

        this.platform.registerCommands(this);
        this.platform.registerListeners(this);

        this.platform.onEnable();
        this.platform.getLogger().info("ChatGames enabled!");
    }

    public void disable() {
        if (this.gameManager != null) this.gameManager.shutdown();
        this.platform.onDisable();
        this.platform.getLogger().info("ChatGames disabled!");
    }

    public void reload() {
        this.platform.getLogger().info("Reloading ChatGames...");
        this.platform.reloadConfig();
        this.configManager.load();
        this.gameManager.reload();
        this.platform.getLogger().info("ChatGames reloaded successfully");
    }

    public void sendMessage(final UUID uuid, final Component component) {
        this.platform.sendMessage(uuid, component);
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
}