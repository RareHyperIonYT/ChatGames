package dev.rarehyperion.chatgames.afk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of the AFK provider registry.
 *
 * <p>
 *     Maintains a thread-safe collection of providers and provides
 *     sorted access by priority.
 * </p>
 *
 * @author tannerharkin
 */
public final class AfkProviderRegistryImpl implements AfkProviderRegistry {

    private final Map<String, AfkProvider> providers = new ConcurrentHashMap<>();

    @Override
    public void register(final AfkProvider provider) {
        this.providers.put(provider.id(), provider);
    }

    @Override
    public boolean unregister(final String providerId) {
        return this.providers.remove(providerId) != null;
    }

    @Override
    public Optional<AfkProvider> getProvider(final String providerId) {
        return Optional.ofNullable(this.providers.get(providerId));
    }

    @Override
    public List<AfkProvider> getProviders() {
        return this.providers.values().stream()
                .sorted(Comparator.comparingInt(AfkProvider::priority))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public List<AfkProvider> getAvailableProviders() {
        return this.providers.values().stream()
                .filter(AfkProvider::isAvailable)
                .sorted(Comparator.comparingInt(AfkProvider::priority))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

}
