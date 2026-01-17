package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.afk.AfkProviderRegistry;
import dev.rarehyperion.chatgames.afk.providers.NucleusAfkProvider;
import dev.rarehyperion.chatgames.config.Config;
import dev.rarehyperion.chatgames.config.SpongeConfig;
import dev.rarehyperion.chatgames.game.EndReason;
import dev.rarehyperion.chatgames.game.GameType;
import dev.rarehyperion.chatgames.listener.SpongeChatListener;
import dev.rarehyperion.chatgames.platform.*;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpongePlatform implements Platform {

    private final PluginContainer container;
    private final PlatformLogger logger;
    private final PlatformPluginMeta meta;

    private final Path defaultConfigPath;
    private final ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode rootNode;

    private final Path privateConfigDir;

    public SpongePlatform(final PluginContainer container, final Logger logger, final Path defaultConfigPath, final ConfigurationLoader<CommentedConfigurationNode> configLoader, final Path privateConfigDir) {
        this.container = container;
        this.logger = new SpongePlatformLogger(logger);
        this.meta = new SpongePluginMeta(container.metadata());

        this.defaultConfigPath = defaultConfigPath;
        this.configLoader = configLoader;
        this.privateConfigDir = privateConfigDir;
    }

    @Override
    public String name() {
        return "SPONGE";
    }

    @Override
    public PlatformPluginMeta pluginMeta() {
        return this.meta;
    }

    @Override
    public void broadcast(final Component component) {
        Sponge.server().onlinePlayers().forEach(player -> player.sendMessage(component));
    }

    @Override
    public void dispatchCommand(final String command) {
        try {
            Sponge.server().commandManager().process(command);
        } catch (final Exception exception) {
            this.logger.warn("Failed to dispatch command: '" + command + "': " + exception.getMessage());
        }
    }

    @Override
    public void registerCommands(final ChatGamesCore core) {}

    @Override
    public void registerListeners(final ChatGamesCore core) {
        Sponge.eventManager().registerListeners(this.container, new SpongeChatListener(core.gameManager()));
    }

    @Override
    public Collection<UUID> getOnlinePlayers() {
        return Sponge.server().onlinePlayers().stream()
                .map(Identifiable::uniqueId)
                .collect(Collectors.toList());
    }

    @Override
    public PlatformTask runTask(final Runnable runnable) {
        final ScheduledTask scheduledTask = Sponge.server().scheduler().submit(
                Task.builder()
                        .plugin(this.container)
                        .execute(runnable)
                        .build()
        );
        return new SpongePlatformTask(scheduledTask);
    }

    @Override
    public PlatformTask runTaskLater(final Runnable runnable, final long delay) {
        final ScheduledTask scheduledTask = Sponge.server().scheduler().submit(
                Task.builder()
                        .plugin(this.container)
                        .execute(runnable)
                        .delay(Ticks.of(delay)) // must use ticks, otherwise timing is incorrect for some reason.
                        .build()
        );
        return new SpongePlatformTask(scheduledTask);
    }

    @Override
    public PlatformTask runTaskTimer(final Runnable runnable, final long initialDelay, final long periodTicks) {
        final ScheduledTask scheduledTask = Sponge.server().scheduler().submit(
                Task.builder()
                        .plugin(this.container)
                        .execute(runnable)
                        .delay(Ticks.of(initialDelay)) // must use ticks, otherwise timing is incorrect for some reason.
                        .interval(Ticks.of(periodTicks)) // must use ticks, otherwise timing is incorrect for some reason.
                        .build()
        );
        return new SpongePlatformTask(scheduledTask);
    }

    @Override
    public void saveDefaultConfig() {
        try {
            Files.createDirectories(this.defaultConfigPath.getParent());

            if(Files.notExists(this.defaultConfigPath)) {
                try (final InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    if (in != null) {
                        Files.copy(in, this.defaultConfigPath);
                        this.logger.info("Default config.yml copied to " + this.defaultConfigPath);
                    } else {
                        this.logger.warn("config.yml not found in JAR!");
                    }
                }
            }

            this.rootNode = this.configLoader.load();
        } catch (final IOException exception) {
            this.logger.warn("Failed to save/load default config");
            exception.printStackTrace(System.err);
        }
    }

    @Override
    public void reloadConfig() {
        try {
            if (Files.notExists(this.defaultConfigPath)) {
                saveDefaultConfig();
            }

            this.rootNode = this.configLoader.load();
        } catch (final IOException exception) {
            this.logger.warn("Failed to reload config");
            exception.printStackTrace(System.err);
        }
    }

    @Override
    public PlatformSender wrapSender(final Object sender) {
        return new SpongePlatformSender(sender);
    }

    @Override
    public <T> T getConfigValue(final String path, final Class<T> type, final T defaultValue) {
        try {
            final Object[] nodes = path.split("\\.");
            final T value = this.rootNode.node(nodes).get(type, defaultValue);
            return value != null ? value : defaultValue;
        } catch (final Exception exception) {
            this.logger.warn("Failed to read config value at '" + path + "': " + exception.getMessage());
            return defaultValue;
        }
    }

    @Override
    public void setConfigValue(final String path, final Object value) {
        try {
            this.rootNode.node((Object[]) path.split("\\.")).set(value);
        } catch (final Exception exception) {
            this.logger.warn("Failed to set config value: '" + path + "': " + exception.getMessage());
        }
    }

    @Override
    public void saveConfig() {
        try {
            this.configLoader.save(this.rootNode);
        } catch (final Exception exception) {
            this.logger.warn("Failed to save config: " + exception.getMessage());
        }
    }

    @Override
    public Config loadConfig(final File file) {
        try {
            final CommentedConfigurationNode node = YamlConfigurationLoader.builder().file(file).build().load(ConfigurationOptions.defaults());
            return new SpongeConfig(node);
        } catch (final ConfigurateException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public File getDataFolder() {
        try {
            Files.createDirectories(this.privateConfigDir);
        } catch (final IOException exception) {
            this.logger.warn("Failed to create data folder: " + this.privateConfigDir);
            exception.printStackTrace(System.err);
        }

        return this.privateConfigDir.toFile();
    }

    @Override
    public InputStream getResource(final String resourcePath) {
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) this.logger.warn("Resource '" + resourcePath + "' not found in JAR");
        return inputStream;
    }

    @Override
    public PlatformLogger getLogger() {
        return this.logger;
    }

    @Override
    public void dispatchStart(final GameType type, final String question, final String answer, final List<String> rewards) {}

    @Override
    public void dispatchWin(final PlatformPlayer pp, final GameType type, final String question, final String answer, final List<String> rewards) {}

    @Override
    public void dispatchEnd(final GameType type, final String question, final String answer, final List<String> rewards, final EndReason reason) {}

    @Override
    public void registerAfkProviders(final AfkProviderRegistry registry) {
        registry.register(new NucleusAfkProvider());
    }

}
