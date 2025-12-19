package dev.rarehyperion.chatgames.platform;

import dev.rarehyperion.chatgames.ChatGamesCore;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

public interface Platform {

    String name();
    PlatformPluginMeta pluginMeta();

    void sendMessage(final UUID recipientUuid, final Component component);
    void broadcast(final Component component);
    void sendConsole(final Component component);

    void dispatchCommand(final String command);

    void registerCommands(final ChatGamesCore core);
    void registerListeners(final ChatGamesCore core);

    Collection<UUID> getOnlinePlayers();

    PlatformTask runTask(final Runnable task);
    PlatformTask runTaskLater(final Runnable task, final long delay);
    PlatformTask runTaskTimer(final Runnable task, final long initialDelay, final long periodTicks);
    PlatformTask runTaskAsync(final Runnable task);

    void saveDefaultConfig();
    void reloadConfig();

    PlatformSender wrapSender(final Object sender);

    <T> T getConfigValue(final String path, Class<T> type, T defaultValue);
    void setConfigValue(final String path, final Object value);
    void saveConfig();

    File getDataFolder();
    InputStream getResource(final String resourcePath);

    String playerName(final UUID uuid);

    default void onLoad() {}
    default void onEnable() {}
    default void onDisable() {}

    Logger getLogger();

}
