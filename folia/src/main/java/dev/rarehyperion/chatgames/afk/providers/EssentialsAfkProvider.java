package dev.rarehyperion.chatgames.afk.providers;

import dev.rarehyperion.chatgames.afk.AfkCheckResult;
import dev.rarehyperion.chatgames.afk.AfkProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * AFK provider for EssentialsX plugin.
 *
 * <p>
 *     Uses reflection to integrate with EssentialsX without requiring
 *     a compile-time dependency.
 * </p>
 *
 * @author tannerharkin
 */
public final class EssentialsAfkProvider implements AfkProvider {

    private Plugin essentialsPlugin;
    private Method getUserMethod;
    private Method isAfkMethod;
    private boolean initialized = false;
    private boolean available = false;

    @Override
    public String id() {
        return "essentialsx";
    }

    @Override
    public String displayName() {
        return "EssentialsX";
    }

    @Override
    public int priority() {
        return 100;
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
            // Get the User object from Essentials
            final Object user = this.getUserMethod.invoke(this.essentialsPlugin, playerId);
            if (user == null) {
                return AfkCheckResult.UNKNOWN;
            }

            // Check if the user is AFK
            final Boolean isAfk = (Boolean) this.isAfkMethod.invoke(user);
            return isAfk ? AfkCheckResult.AFK : AfkCheckResult.ACTIVE;
        } catch (final Exception exception) {
            return AfkCheckResult.UNKNOWN;
        }
    }

    private void initialize() {
        this.initialized = true;

        this.essentialsPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (this.essentialsPlugin == null || !this.essentialsPlugin.isEnabled()) {
            return;
        }

        try {
            // Get the Essentials class and getUser method
            final Class<?> essentialsClass = this.essentialsPlugin.getClass();
            this.getUserMethod = essentialsClass.getMethod("getUser", UUID.class);

            // Get the User class and isAfk method
            final Class<?> userClass = this.getUserMethod.getReturnType();
            this.isAfkMethod = userClass.getMethod("isAfk");

            this.available = true;
        } catch (final Exception exception) {
            this.available = false;
        }
    }

}
