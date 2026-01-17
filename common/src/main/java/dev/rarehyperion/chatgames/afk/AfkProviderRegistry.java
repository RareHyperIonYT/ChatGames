package dev.rarehyperion.chatgames.afk;

import java.util.List;
import java.util.Optional;

/**
 * Registry for AFK detection providers.
 *
 * <p>
 *     External plugins can register custom providers through this interface.
 *     Providers are automatically sorted by priority when retrieved.
 * </p>
 *
 * @author tannerharkin
 */
public interface AfkProviderRegistry {

    /**
     * Registers an AFK provider.
     *
     * <p>
     *     If a provider with the same ID already exists, it will be replaced.
     * </p>
     *
     * @param provider The provider to register
     */
    void register(AfkProvider provider);

    /**
     * Unregisters an AFK provider by its ID.
     *
     * @param providerId The ID of the provider to unregister
     * @return true if a provider was removed, false if no provider had that ID
     */
    boolean unregister(String providerId);

    /**
     * Gets a provider by its ID.
     *
     * @param providerId The ID of the provider
     * @return An Optional containing the provider, or empty if not found
     */
    Optional<AfkProvider> getProvider(String providerId);

    /**
     * Gets all registered providers sorted by priority (lowest first).
     *
     * @return An unmodifiable list of providers
     */
    List<AfkProvider> getProviders();

    /**
     * Gets all available providers sorted by priority (lowest first).
     *
     * <p>
     *     Only returns providers where {@link AfkProvider#isAvailable()} returns true.
     * </p>
     *
     * @return An unmodifiable list of available providers
     */
    List<AfkProvider> getAvailableProviders();

}
