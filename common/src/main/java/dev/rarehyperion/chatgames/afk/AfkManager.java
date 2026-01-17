package dev.rarehyperion.chatgames.afk;

import dev.rarehyperion.chatgames.afk.providers.DefaultAfkProvider;
import dev.rarehyperion.chatgames.platform.Platform;
import dev.rarehyperion.chatgames.platform.PlatformLogger;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Manages AFK detection across different providers.
 *
 * <p>
 *     Coordinates the evaluation of AFK providers and exposes methods
 *     for checking individual player status and counting active players.
 * </p>
 *
 * @author tannerharkin
 */
public final class AfkManager {

    private final Platform platform;
    private final AfkProviderRegistryImpl registry;
    private final PlatformLogger logger;

    private boolean enabled;
    private Set<String> allowedProviders;

    public AfkManager(final Platform platform) {
        this.platform = platform;
        this.registry = new AfkProviderRegistryImpl();
        this.logger = platform.getLogger();

        // Register the default provider (always returns ACTIVE)
        this.registry.register(new DefaultAfkProvider());
    }

    /**
     * Loads configuration settings for AFK detection.
     *
     * @param enabled Whether AFK detection is enabled
     * @param allowedProviders Set of allowed provider IDs (empty means all providers)
     */
    public void configure(final boolean enabled, final Set<String> allowedProviders) {
        this.enabled = enabled;
        this.allowedProviders = allowedProviders;

        if (this.enabled) {
            final List<AfkProvider> available = this.registry.getAvailableProviders();
            if (available.isEmpty()) {
                this.logger.warn("AFK detection enabled but no providers are available!");
            } else {
                this.logger.info("AFK detection enabled with " + available.size() + " provider(s):");
                for (final AfkProvider provider : available) {
                    if (this.isProviderAllowed(provider)) {
                        this.logger.info("  - " + provider.displayName() + " (priority: " + provider.priority() + ")");
                    }
                }
            }
        }
    }

    /**
     * Gets the provider registry for external registration.
     *
     * @return The provider registry
     */
    public AfkProviderRegistry getRegistry() {
        return this.registry;
    }

    /**
     * Checks if a player should be counted (not AFK).
     *
     * @param playerId The UUID of the player
     * @return true if the player is active (not AFK), false if AFK
     */
    public boolean shouldCountPlayer(final UUID playerId) {
        if (!this.enabled) {
            return true; // AFK detection disabled, count all players
        }

        final AfkCheckResult result = this.checkPlayerAfk(playerId);
        return result != AfkCheckResult.AFK;
    }

    /**
     * Checks the AFK status of a player using the provider chain.
     *
     * @param playerId The UUID of the player
     * @return The AFK check result
     */
    public AfkCheckResult checkPlayerAfk(final UUID playerId) {
        final boolean debug = this.platform.getConfigValue("debug", Boolean.class, false);

        for (final AfkProvider provider : this.registry.getAvailableProviders()) {
            if (!this.isProviderAllowed(provider)) {
                continue;
            }

            try {
                final AfkCheckResult result = provider.checkAfk(playerId);

                if (debug) {
                    this.logger.info("[AFK Debug] Provider '" + provider.id() + "' returned " + result + " for " + playerId);
                }

                if (result != AfkCheckResult.UNKNOWN) {
                    return result;
                }
            } catch (final Exception exception) {
                this.logger.warn("AFK provider '" + provider.id() + "' threw exception: " + exception.getMessage());
            }
        }

        // Fallback to ACTIVE if no provider could determine status
        return AfkCheckResult.ACTIVE;
    }

    /**
     * Gets the count of active (non-AFK) players.
     *
     * @return The number of active players
     */
    public int getActivePlayerCount() {
        if (!this.enabled) {
            return this.platform.getOnlinePlayers().size();
        }

        final Collection<UUID> onlinePlayers = this.platform.getOnlinePlayers();
        int activeCount = 0;

        for (final UUID playerId : onlinePlayers) {
            if (this.shouldCountPlayer(playerId)) {
                activeCount++;
            }
        }

        return activeCount;
    }

    /**
     * Checks if a provider is allowed based on configuration.
     *
     * @param provider The provider to check
     * @return true if the provider is allowed
     */
    private boolean isProviderAllowed(final AfkProvider provider) {
        if (this.allowedProviders == null || this.allowedProviders.isEmpty()) {
            return true; // Empty list means all providers allowed
        }
        return this.allowedProviders.contains(provider.id());
    }

    /**
     * Returns whether AFK detection is enabled.
     *
     * @return true if AFK detection is enabled
     */
    public boolean isEnabled() {
        return this.enabled;
    }

}
