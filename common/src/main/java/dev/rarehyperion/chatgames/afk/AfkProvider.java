package dev.rarehyperion.chatgames.afk;

import java.util.UUID;

/**
 * Interface for AFK detection providers.
 *
 * <p>
 *     Implementations can integrate with external plugins (EssentialsX, CMI, etc.)
 *     or use platform-specific APIs (Paper's idle detection) to determine if a
 *     player is AFK.
 * </p>
 *
 * <p>
 *     Providers are evaluated in priority order (lowest number = highest priority).
 *     When a provider returns {@link AfkCheckResult#UNKNOWN}, the next provider
 *     in the chain is consulted.
 * </p>
 *
 * @author tannerharkin
 */
public interface AfkProvider {

    /**
     * Returns the unique identifier for this provider.
     *
     * @return The provider ID (e.g., "essentialsx", "paper-idle")
     */
    String id();

    /**
     * Returns the human-readable display name for this provider.
     *
     * @return The display name (e.g., "EssentialsX", "Paper Idle API")
     */
    String displayName();

    /**
     * Returns the priority of this provider.
     * Lower values indicate higher priority.
     *
     * <p>Recommended priority ranges:</p>
     * <ul>
     *     <li>0-99: External plugins (custom registrations)</li>
     *     <li>100: EssentialsX</li>
     *     <li>110: CMI</li>
     *     <li>120: AntiAFKPlus</li>
     *     <li>150: Nucleus (Sponge)</li>
     *     <li>200: Paper Idle API</li>
     *     <li>1000: Default (always returns ACTIVE)</li>
     * </ul>
     *
     * @return The priority value
     */
    int priority();

    /**
     * Checks if this provider is available and can function.
     *
     * <p>
     *     For plugin-based providers, this typically checks if the required
     *     plugin is installed and enabled.
     * </p>
     *
     * @return true if the provider can check AFK status, false otherwise
     */
    boolean isAvailable();

    /**
     * Checks the AFK status of a player.
     *
     * @param playerId The UUID of the player to check
     * @return The AFK check result (ACTIVE, AFK, or UNKNOWN)
     */
    AfkCheckResult checkAfk(UUID playerId);

}
