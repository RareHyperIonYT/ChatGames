package dev.rarehyperion.chatgames;

import dev.rarehyperion.chatgames.afk.AfkManager;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.game.GameManager;
import dev.rarehyperion.chatgames.game.GameRegistry;
import dev.rarehyperion.chatgames.platform.Platform;
import dev.rarehyperion.chatgames.versioning.VersionChecker;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ChatGamesCore {

    private final Platform platform;

    private ConfigManager configManager;
    private AfkManager afkManager;
    private GameRegistry gameRegistry;
    private GameManager gameManager;

    public ChatGamesCore(final Platform platform) {
        this.platform = platform;
    }

    public void load() {
        this.platform.saveDefaultConfig();
    }

    public void enable() {
        this.platform.getLogger().info("Running on platform: " + this.platform.name());

        this.configManager = new ConfigManager(this.platform);
        this.afkManager = new AfkManager(this.platform);
        this.gameRegistry = new GameRegistry(this);
        this.gameManager = new GameManager(this, this.configManager, this.gameRegistry);

        this.configManager.load();

        // Initialize AFK detection
        this.platform.registerAfkProviders(this.afkManager.getRegistry());
        this.configureAfkManager();

        this.gameRegistry.registerDefaults();
        this.gameRegistry.loadGames();

        this.gameManager.startScheduler();

        this.platform.registerCommands(this);
        this.platform.registerListeners(this);

        VersionChecker.check(this.platform);

        this.platform.getLogger().info("ChatGames enabled!");

    }

    public void disable() {
        if (this.gameManager != null) this.gameManager.shutdown();
        this.platform.getLogger().info("ChatGames disabled!");
    }

    public void reload() {
        this.platform.getLogger().info("Reloading ChatGames...");
        this.platform.reloadConfig();
        this.configManager.load();
        this.configureAfkManager();
        this.gameManager.reload();
        this.platform.getLogger().info("ChatGames reloaded successfully");
    }

    private void configureAfkManager() {
        final boolean enabled = this.platform.getConfigValue("afk-detection.enabled", Boolean.class, false);

        // Get allowed providers from config (empty list means all providers)
        final List<?> providerList = this.platform.getConfigValue("afk-detection.providers", List.class, Collections.emptyList());
        final Set<String> allowedProviders = new HashSet<>();
        for (final Object provider : providerList) {
            if (provider instanceof String) {
                allowedProviders.add((String) provider);
            }
        }

        this.afkManager.configure(enabled, allowedProviders);
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

    public AfkManager afkManager() {
        return this.afkManager;
    }

}