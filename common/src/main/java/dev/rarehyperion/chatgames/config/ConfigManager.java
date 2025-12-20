package dev.rarehyperion.chatgames.config;

import dev.rarehyperion.chatgames.platform.Platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public final class ConfigManager {

    private final Platform plugin;
    private final Map<String, String> messages = new HashMap<>();
    private PluginSettings settings;

    public ConfigManager(final Platform plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.settings = new PluginSettings(
                this.plugin.getConfigValue("game-interval", Integer.class, 3000),
                this.plugin.getConfigValue("minimum-players", Integer.class, 1),
                this.plugin.getConfigValue("automatic-games", Boolean.class, true),
                this.plugin.getConfigValue("answer-cooldown-ticks", Integer.class, 60),
                this.plugin.getConfigValue("debug", Boolean.class, false)
        );

        this.loadLanguage(this.plugin.getConfigValue("languages", String.class, "en-us"));
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

        final Config langConfig = this.plugin.loadConfig(languageFile);

        for(final String key : langConfig.getKeys(false)) {
            this.messages.put(key, langConfig.getString(key, "<red>Failed to fetch message from language!</red>"));
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
            this.plugin.getLogger().error("Failed to save resource: " + resourcePath);
        }
    }

    public String getMessage(final String key, final String defaultValue) {
        return this.messages.getOrDefault(key, defaultValue);
    }

    public PluginSettings getSettings() {
        return this.settings;
    }

    public static final class PluginSettings {

        private final int gameInterval;
        private final int minimumPlayers;
        private final boolean automaticGames;
        private final int answerCooldownTicks;
        private final boolean debug;

        public PluginSettings(int gameInterval, int minimumPlayers, boolean automaticGames, int answerCooldownTicks, boolean debug) {
            this.gameInterval = gameInterval;
            this.minimumPlayers = minimumPlayers;
            this.automaticGames = automaticGames;
            this.answerCooldownTicks = answerCooldownTicks;
            this.debug = debug;
        }

        public int gameInterval() {
            return this.gameInterval;
        }

        public int minimumPlayers() {
            return this.minimumPlayers;
        }

        public boolean automaticGames() {
            return this.automaticGames;
        }

        public int answerCooldownTicks() {
            return this.answerCooldownTicks;
        }

        public boolean debug() {
            return this.debug;
        }

    }

}