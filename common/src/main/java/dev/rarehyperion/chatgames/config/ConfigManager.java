package dev.rarehyperion.chatgames.config;

import dev.rarehyperion.chatgames.AbstractChatGames;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public final class ConfigManager {

    private final AbstractChatGames plugin;
    private final Map<String, String> messages = new HashMap<>();
    private PluginSettings settings;

    public ConfigManager(final AbstractChatGames plugin) {
        this.plugin = plugin;
    }

    public void load() {
        final FileConfiguration config = this.plugin.getConfig();

        this.settings = new PluginSettings(
                config.getInt("game-interval", 3000),
                config.getInt("minimum-players", 1),
                config.getBoolean("automatic-games", true),
                config.getInt("answer-cooldown-ticks", 60),
                config.getBoolean("debug", false)
        );

        this.loadLanguage(config.getString("languages", "en-us"));
    }

    private void loadLanguage(final String languageCode) {
        this.messages.clear();

        final File languageFolder = new File(this.plugin.getDataFolder(), "languages");

        if(!languageFolder.exists()) {
            this.createDefaultLanguages(languageFolder);
        }

        File languageFile = new File(languageFolder, languageCode + ".yml");

        if(!languageFile.exists()) {
            this.createDefaultLanguages(languageFolder);

            if(!languageFile.exists()) {
                languageFile = new File(languageFolder, "en-us.yml");
            }
        }

        final FileConfiguration langConfig = YamlConfiguration.loadConfiguration(languageFile);

        for(final String key : langConfig.getKeys(false)) {
            this.messages.put(key, langConfig.getString(key));
        }

        this.plugin.getLogger().info("Loaded language: " + languageCode.toUpperCase());
    }

    private void createDefaultLanguages(final File folder) {
        if(!folder.exists() && !folder.mkdirs()) throw new IllegalStateException("Unable to create language folder.");
        this.saveResource("languages/en-us.yml", folder);
    }

    private void saveResource(final String resourcePath, final File folder) {
        try (final InputStream stream = this.plugin.getResource(resourcePath)) {
            if (stream != null) {
                File output = new File(folder, new File(resourcePath).getName());
                Files.copy(stream, output.toPath());
            }
        } catch (final IOException e) {
            this.plugin.getLogger().severe("Failed to save resource: " + resourcePath);
        }
    }

    public String getMessage(final String key, final String defaultValue) {
        return this.messages.getOrDefault(key, defaultValue);
    }

    public PluginSettings getSettings() {
        return this.settings;
    }

    public record PluginSettings(
            int gameInterval,
            int minimumPlayers,
            boolean automaticGames,
            int answerCooldownTicks,
            boolean debug
    ) {}

}