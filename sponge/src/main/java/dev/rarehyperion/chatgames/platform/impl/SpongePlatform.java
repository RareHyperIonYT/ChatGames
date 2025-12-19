package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.command.SpongeChatGamesCommand;
import dev.rarehyperion.chatgames.listener.SpongeChatListener;
import dev.rarehyperion.chatgames.platform.Platform;
import dev.rarehyperion.chatgames.platform.PlatformPluginMeta;
import dev.rarehyperion.chatgames.platform.PlatformSender;
import dev.rarehyperion.chatgames.platform.PlatformTask;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EventListener;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SpongePlatform implements Platform {

    private final PluginContainer container;
    private final Logger logger;
    private final PlatformPluginMeta meta;

    private final Path defaultConfigPath;
    private final ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode rootNode;

    private final Path privateConfigDir;

    public SpongePlatform(final PluginContainer container, final Logger logger, final Path defaultConfigPath, final ConfigurationLoader<CommentedConfigurationNode> configLoader, final Path privateConfigDir) {
        this.container = container;
        this.logger = logger;
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
    public void sendMessage(final UUID recipientUuid, final Component component) {
        Sponge.server().player(recipientUuid).ifPresent(player -> player.sendMessage(component));
    }

    @Override
    public void broadcast(final Component component) {
        Sponge.server().onlinePlayers().forEach(player -> player.sendMessage(component));
    }

    @Override
    public void sendConsole(final Component component) {}

    @Override
    public void dispatchCommand(final String command) {
        try {
            Sponge.server().commandManager().process(command);
        } catch (final Exception exception) {
            this.logger.warn("Failed to dispatch command '{}': {}", command, exception.getMessage());
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
                .toList();
    }

    @Override
    public PlatformTask runTask(Runnable runnable) {
        final ScheduledTask scheduledTask = Sponge.server().scheduler().submit(
                Task.builder()
                        .execute(runnable)
                        .build()
        );
        return new SpongePlatformTask(scheduledTask);
    }

    @Override
    public PlatformTask runTaskLater(Runnable runnable, long delay) {
        final ScheduledTask scheduledTask = Sponge.server().scheduler().submit(
                Task.builder()
                        .execute(runnable)
                        .delay(delay, TimeUnit.MILLISECONDS)
                        .build()
        );
        return new SpongePlatformTask(scheduledTask);
    }

    @Override
    public PlatformTask runTaskTimer(Runnable runnable, long initialDelay, long periodTicks) {
        final ScheduledTask scheduledTask = Sponge.server().scheduler().submit(
                Task.builder()
                        .execute(runnable)
                        .delay(initialDelay, TimeUnit.MILLISECONDS)
                        .interval(periodTicks, TimeUnit.MILLISECONDS)
                        .build()
        );
        return new SpongePlatformTask(scheduledTask);
    }

    @Override
    public PlatformTask runTaskAsync(Runnable runnable) {
        final ScheduledTask scheduledTask = Sponge.server().scheduler().submit(
                Task.builder()
                        .execute(runnable)
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
                        this.logger.info("Default config.yml copied to {}", this.defaultConfigPath);
                    } else {
                        this.logger.warn("config.yml not found in JAR!");
                    }
                }
            }

            this.configLoader.load();
        } catch (final IOException exception) {
            this.logger.warn("Failed to save/load default config", exception);
        }
    }

    @Override
    public void reloadConfig() {
        try {
            if (Files.notExists(this.defaultConfigPath)) {
                saveDefaultConfig();
            }

            this.rootNode = this.configLoader.load();
        } catch (IOException e) {
            logger.warn("Failed to reload config", e);
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
            this.logger.warn("Failed to read config value at {}: {}", path, exception.getMessage());
            return defaultValue;
        }
    }

    @Override
    public void setConfigValue(final String path, final Object value) {
        try {
            this.rootNode.node((Object[]) path.split("\\.")).set(value);
        } catch (final Exception exception) {
            this.logger.warn("Failed to set config value: {}: {}", path, exception.getMessage());
        }
    }

    @Override
    public void saveConfig() {
        try {
            this.configLoader.save(this.rootNode);
        } catch (final Exception exception) {
            this.logger.warn("Failed to save config: {}", exception.getMessage());
        }
    }

    @Override
    public File getDataFolder() {
        try {
            Files.createDirectories(this.privateConfigDir);
        } catch (final IOException exception) {
            this.logger.warn("Failed to create data folder: {}", this.privateConfigDir, exception);
        }

        return this.privateConfigDir.toFile();
    }

    @Override
    public InputStream getResource(final String resourcePath) {
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) this.logger.warn("Resource {} not found in JAR", resourcePath);
        return inputStream;
    }

    @Override
    public String playerName(UUID uuid) {
        return Sponge.server().player(uuid).get().name();
    }

    @Override
    public java.util.logging.Logger getLogger() {
        return java.util.logging.Logger.getLogger(this.meta.getName());
    }
}
