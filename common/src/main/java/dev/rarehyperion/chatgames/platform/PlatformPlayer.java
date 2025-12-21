package dev.rarehyperion.chatgames.platform;

import net.kyori.adventure.text.Component;

import java.util.UUID;

/**
 * Represents a player on the platform.
 *
 * <p>
 *     Provides methods for sending messages, and accessing the player identity.
 * </p>
 *
 * @author RareHyperIon
 * @since 20/12/2025
 */
public interface PlatformPlayer {

    /**
     * Sends a chat message to the player.
     * @param component The message to send.
     */
    void sendMessage(final Component component);

    /**
     * Returns the name of the player.
     * @return The player's name.
     */
    String name();

    /**
     * Returns the unique identifier (UUID) of the player.
     * @return The player's UUID.
     */
    UUID id();

}
