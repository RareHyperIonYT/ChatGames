package dev.rarehyperion.chatgames.platform;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.config.Config;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

public interface Platform {

    String name();
    PlatformPluginMeta pluginMeta();

    void broadcast(final Component component);

    void dispatchCommand(final String command);

    void registerCommands(final ChatGamesCore core);
    void registerListeners(final ChatGamesCore core);

    Collection<UUID> getOnlinePlayers();

    PlatformTask runTask(final Runnable task);
    PlatformTask runTaskLater(final Runnable task, final long delay);
    PlatformTask runTaskTimer(final Runnable task, final long initialDelay, final long periodTicks);

    void saveDefaultConfig();
    void reloadConfig();

    PlatformSender wrapSender(final Object sender);

    <T> T getConfigValue(final String path, Class<T> type, T defaultValue);
    void setConfigValue(final String path, final Object value);
    void saveConfig();

    Config loadConfig(final File file);

    File getDataFolder();
    InputStream getResource(final String resourcePath);

    default void onLoad() {}
    default void onEnable() {}
    default void onDisable() {}

    Logger getLogger();

}
