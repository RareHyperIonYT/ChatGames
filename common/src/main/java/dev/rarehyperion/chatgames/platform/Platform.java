package dev.rarehyperion.chatgames.platform;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.config.Config;
import dev.rarehyperion.chatgames.game.EndReason;
import dev.rarehyperion.chatgames.game.GameType;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Represents a platform abstraction for running ChatGames.
 *
 * <p>
 *     Implementations handle differences between server types (e.g., Spigot, Paper, Sponge) and provie methods for broadcasting messages,
 *     scheduling tasks, accessing configurations, registering commands and listeners, and wrapping platform-specific senders.
 * </p>
 *
 * @author RareHyperIon
 * @since 20/12/2025
 */
public interface Platform {

    /**
     * Returns the name of the platform, typically in uppercase (e.g., "SPIGOT").
     * @return The platform name.
     */
    String name();

    /**
     * Returns the metadata bout the running plugin.
     * @return The plugin metadata.
     */
    PlatformPluginMeta pluginMeta();

    /**
     * Broadcasts a message to all the online players on the platform.
     * @param component The message to broadcast.
     */
    void broadcast(final Component component);

    /**
     * Dispatches a command as if executed by the server console.
     * @param command The command to execute.
     */
    void dispatchCommand(final String command);

    /**
     * Registers commands required by the ChatGames plugin.
     * @param core The main ChatGames instance.
     */
    void registerCommands(final ChatGamesCore core);

    /**
     * Registers listeners required by the ChatGames plugin.
     * @param core The main ChatGames instance.
     */
    void registerListeners(final ChatGamesCore core);

    /**
     * Returns a collection of all currently online player UUIDs.
     * @return The collection of online player UUIDs.
     */
    Collection<UUID> getOnlinePlayers();

    /**
     * Runs a task immediately on the platform's scheduler.
     *
     * @param task The task to run.
     * @return A handle to the scheduled task.
     */
    PlatformTask runTask(final Runnable task);

    /**
     * Runs a task after a delay on the platform's scheduler
     *
     * @param task  The task to run.
     * @param delay The delay before running.
     * @return A handle to the scheduled task.
     */
    PlatformTask runTaskLater(final Runnable task, final long delay);

    /**
     * Runs a task repeatedly on the platform's schedular.
     *
     * @param task         The task to run.
     * @param initialDelay The delay before running.
     * @param periodTicks  The delay between subsequent executions.
     * @return A handle to the scheduled task.
     */
    PlatformTask runTaskTimer(final Runnable task, final long initialDelay, final long periodTicks);

    /**
     * Saves the plugin's default configuration file, if it does not exist.
     */
    void saveDefaultConfig();

    /**
     * Reloads the plugin's configuration from disk.
     */
    void reloadConfig();

    /**
     * Wraps a platform-specific sender object (e.g., CommandSender in Spigot) into a {@link PlatformSender} for uniform interaction.
     *
     * @param sender The sender object.
     * @return The wrapped PlatformSender
     *
     * @throws IllegalArgumentException if the sender type is unsupported.
     */
    PlatformSender wrapSender(final Object sender);

    /**
     * Retrieves a configuration value by a path.
     *
     * @param path         The configuration path.
     * @param type         The expected type of value.
     * @param defaultValue The default value to return if the path does not exist or is invalid.
     * @param <T>          The type of the value.
     * @return             The configuration value, or the default if missing or invalid.
     */
    <T> T getConfigValue(final String path, final Class<T> type, final T defaultValue);

    /**
     * Sets a configuration value at the specified path.
     *
     * @param path  The configuration path/
     * @param value The value to set.
     */
    void setConfigValue(final String path, final Object value);

    /**
     * Saves the plugin's configuration to disk.
     */
    void saveConfig();

    /**
     * Loads a configuration from file.
     *
     * @param file The file to load.
     * @return A {@link Config} object representing the loaded configuration.
     */
    Config loadConfig(final File file);

    /**
     * Returns the plugin's data folder.
     * @return The data folder.
     */
    File getDataFolder();

    /**
     * Retrieves a resource from the plugin's jar.
     * @param resourcePath The path of the resource.
     * @return             An {@link InputStream} of the resource, or null if not found.
     */
    InputStream getResource(final String resourcePath);

    /**
     * Returns the platform's logger for outputting messages.
     * @return The platform logger.
     */
    PlatformLogger getLogger();

    void dispatchStart(final GameType type, final String question, final String answer, final List<String> rewards);
    void dispatchEnd(final GameType type, final String question, final String answer, final List<String> rewards, final EndReason reason);
    void dispatchWin(final PlatformPlayer player, final GameType type, final String question, final String answer, final List<String> rewards);

}
