package dev.rarehyperion.chatgames.platform;

import net.kyori.adventure.text.Component;

/**
 * Represents a command sender on the platform.
 *
 * <p>
 *     Can be a player, console, or other entity capable of sending commands/messages.
 * </p>
 *
 * @author RareHyperIon
 * @since 20/12/2025
 */
public interface PlatformSender {

    /**
     * Sends a chat message to the sender.
     * @param component The message to send.
     */
    void sendMessage(final Component component);

    /**
     * Checks whether the sender has a specific permission.
     *
     * @param permission The permission to check.
     * @return {@code true} if the sender has the permission, {@code false} otherwise.
     */
    boolean hasPermission(final String permission);

    /**
     * Checks if the sender is the console.
     * @return {@code true} if the sender is the console, {@code false} otherwise.
     */
    boolean isConsole();

    /**
     * Returns the underlying player if this sender represents a player.
     * @return The player instance, or null if the sender is not a player.
     */
    PlatformPlayer player();

}
