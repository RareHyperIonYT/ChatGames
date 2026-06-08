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
        final RedisSettings redisSettings = new RedisSettings(
                this.plugin.getConfigValue("redis.enabled", Boolean.class, false),
                this.plugin.getConfigValue("redis.host", String.class, "127.0.0.1"),
                this.plugin.getConfigValue("redis.port", Integer.class, 6379),
                this.plugin.getConfigValue("redis.username", String.class, ""),
                this.plugin.getConfigValue("redis.password", String.class, ""),
                this.plugin.getConfigValue("redis.database", Integer.class, 0),
                this.plugin.getConfigValue("redis.ssl", Boolean.class, false),
                this.plugin.getConfigValue("redis.timeout-ms", Integer.class, 2000),
                this.plugin.getConfigValue("redis.key-prefix", String.class, "chatgame"),
                this.plugin.getConfigValue("redis.leader-lock-seconds", Integer.class, 15),
                this.plugin.getConfigValue("redis.reward-dedup-seconds", Integer.class, 86400)
        );

        this.settings = new PluginSettings(
                this.plugin.getConfigValue("game-interval", Integer.class, 3000),
                this.plugin.getConfigValue("minimum-players", Integer.class, 1),
                this.plugin.getConfigValue("automatic-games", Boolean.class, true),
                this.plugin.getConfigValue("multi-server.enabled", Boolean.class, false),
                this.plugin.getConfigValue("multi-server.leader", Boolean.class, false),
                this.plugin.getConfigValue("answer-cooldown-ticks", Integer.class, 60),
                this.plugin.getConfigValue("debug", Boolean.class, false),
                redisSettings
        );

        String language = this.plugin.getConfigValue("language", String.class, "");
        if(language.trim().isEmpty()) {
            language = this.plugin.getConfigValue("languages", String.class, "en-us");
        }

        this.loadLanguage(language);
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
        private final boolean multiServerEnabled;
        private final boolean multiServerLeader;
        private final int answerCooldownTicks;
        private final boolean debug;
        private final RedisSettings redisSettings;

        public PluginSettings(final int gameInterval, final int minimumPlayers, final boolean automaticGames,
                              final boolean multiServerEnabled, final boolean multiServerLeader,
                              final int answerCooldownTicks, final boolean debug, final RedisSettings redisSettings) {
            this.gameInterval = gameInterval;
            this.minimumPlayers = minimumPlayers;
            this.automaticGames = automaticGames;
            this.multiServerEnabled = multiServerEnabled;
            this.multiServerLeader = multiServerLeader;
            this.answerCooldownTicks = answerCooldownTicks;
            this.debug = debug;
            this.redisSettings = redisSettings;
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

        public boolean multiServerEnabled() {
            return this.multiServerEnabled;
        }

        public boolean multiServerLeader() {
            return this.multiServerLeader;
        }

        public boolean distributedGames() {
            return this.multiServerEnabled && this.redisSettings.enabled();
        }

        public boolean distributedAutomaticLeader() {
            return this.distributedGames() && this.automaticGames && this.multiServerLeader;
        }

        public int answerCooldownTicks() {
            return this.answerCooldownTicks;
        }

        public boolean debug() {
            return this.debug;
        }

        public RedisSettings redis() {
            return this.redisSettings;
        }

    }

    public static final class RedisSettings {

        private final boolean enabled;
        private final String host;
        private final int port;
        private final String username;
        private final String password;
        private final int database;
        private final boolean ssl;
        private final int timeoutMillis;
        private final String keyPrefix;
        private final int leaderLockSeconds;
        private final int rewardDedupSeconds;

        public RedisSettings(final boolean enabled, final String host, final int port, final String username,
                             final String password, final int database, final boolean ssl, final int timeoutMillis,
                             final String keyPrefix, final int leaderLockSeconds, final int rewardDedupSeconds) {
            this.enabled = enabled;
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.database = database;
            this.ssl = ssl;
            this.timeoutMillis = timeoutMillis;
            this.keyPrefix = keyPrefix;
            this.leaderLockSeconds = leaderLockSeconds;
            this.rewardDedupSeconds = rewardDedupSeconds;
        }

        public boolean enabled() {
            return this.enabled;
        }

        public String host() {
            return this.host;
        }

        public int port() {
            return this.port;
        }

        public String username() {
            return this.username;
        }

        public String password() {
            return this.password;
        }

        public int database() {
            return this.database;
        }

        public boolean ssl() {
            return this.ssl;
        }

        public int timeoutMillis() {
            return this.timeoutMillis;
        }

        public String keyPrefix() {
            return this.keyPrefix;
        }

        public int leaderLockSeconds() {
            return this.leaderLockSeconds;
        }

        public int rewardDedupSeconds() {
            return this.rewardDedupSeconds;
        }

    }

}
