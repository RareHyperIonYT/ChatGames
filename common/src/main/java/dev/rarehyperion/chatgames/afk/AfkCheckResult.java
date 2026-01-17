package dev.rarehyperion.chatgames.afk;

/**
 * Represents the result of an AFK check for a player.
 *
 * <p>
 *     Providers return one of these values when checking a player's AFK status.
 *     UNKNOWN indicates the provider cannot determine the status and the next
 *     provider in the chain should be consulted.
 * </p>
 *
 * @author tannerharkin
 */
public enum AfkCheckResult {

    /**
     * The player is actively playing and not AFK.
     */
    ACTIVE,

    /**
     * The player is away from keyboard.
     */
    AFK,

    /**
     * The provider cannot determine the player's status.
     * The next provider in the chain should be consulted.
     */
    UNKNOWN

}
