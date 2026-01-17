package dev.rarehyperion.chatgames.afk.providers;

import dev.rarehyperion.chatgames.afk.AfkCheckResult;
import dev.rarehyperion.chatgames.afk.AfkProvider;
import org.spongepowered.api.Sponge;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

/**
 * AFK provider for Nucleus plugin on Sponge.
 *
 * <p>
 *     Uses reflection to integrate with Nucleus without requiring
 *     a compile-time dependency.
 * </p>
 *
 * @author tannerharkin
 */
public final class NucleusAfkProvider implements AfkProvider {

    private Object afkService;
    private Method isAfkMethod;
    private boolean initialized = false;
    private boolean available = false;

    @Override
    public String id() {
        return "nucleus";
    }

    @Override
    public String displayName() {
        return "Nucleus";
    }

    @Override
    public int priority() {
        return 150;
    }

    @Override
    public boolean isAvailable() {
        if (!this.initialized) {
            this.initialize();
        }
        return this.available;
    }

    @Override
    public AfkCheckResult checkAfk(final UUID playerId) {
        if (!this.isAvailable()) {
            return AfkCheckResult.UNKNOWN;
        }

        try {
            // Call isAFK(UUID) on the AFKService
            final Object result = this.isAfkMethod.invoke(this.afkService, playerId);

            // Handle the result - Nucleus returns boolean
            if (result instanceof Boolean) {
                return (Boolean) result ? AfkCheckResult.AFK : AfkCheckResult.ACTIVE;
            }

            return AfkCheckResult.UNKNOWN;
        } catch (final Exception exception) {
            return AfkCheckResult.UNKNOWN;
        }
    }

    private void initialize() {
        this.initialized = true;

        try {
            // Check if Nucleus plugin is loaded
            final Optional<?> nucleusContainer = Sponge.pluginManager().plugin("nucleus");
            if (!nucleusContainer.isPresent()) {
                return;
            }

            // Try to get the AFKService from Sponge's service manager
            final Class<?> afkServiceClass = Class.forName("io.github.nucleuspowered.nucleus.api.module.afk.NucleusAFKService");
            final Optional<?> serviceOptional = Sponge.serviceProvider().provide(afkServiceClass);

            if (!serviceOptional.isPresent()) {
                return;
            }

            this.afkService = serviceOptional.get();

            // Get the isAFK method
            this.isAfkMethod = afkServiceClass.getMethod("isAFK", UUID.class);

            this.available = true;
        } catch (final Exception exception) {
            this.available = false;
        }
    }

}
