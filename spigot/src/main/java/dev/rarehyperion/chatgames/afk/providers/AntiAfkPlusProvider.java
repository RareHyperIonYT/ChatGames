package dev.rarehyperion.chatgames.afk.providers;

import dev.rarehyperion.chatgames.afk.AfkCheckResult;
import dev.rarehyperion.chatgames.afk.AfkProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * AFK provider for AntiAFKPlus plugin.
 *
 * <p>
 *     Uses reflection to integrate with AntiAFKPlus without requiring
 *     a compile-time dependency.
 * </p>
 *
 * @author tannerharkin
 */
public final class AntiAfkPlusProvider implements AfkProvider {

    private Plugin antiAfkPlugin;
    private Method getApiMethod;
    private Method isAfkMethod;
    private boolean initialized = false;
    private boolean available = false;

    @Override
    public String id() {
        return "antiafkplus";
    }

    @Override
    public String displayName() {
        return "AntiAFKPlus";
    }

    @Override
    public int priority() {
        return 120;
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

        final Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return AfkCheckResult.UNKNOWN;
        }

        try {
            // Get the API instance
            final Object api = this.getApiMethod.invoke(this.antiAfkPlugin);
            if (api == null) {
                return AfkCheckResult.UNKNOWN;
            }

            // Check if player is AFK
            final Boolean isAfk = (Boolean) this.isAfkMethod.invoke(api, player);
            return isAfk ? AfkCheckResult.AFK : AfkCheckResult.ACTIVE;
        } catch (final Exception exception) {
            return AfkCheckResult.UNKNOWN;
        }
    }

    private void initialize() {
        this.initialized = true;

        this.antiAfkPlugin = Bukkit.getPluginManager().getPlugin("AntiAFKPlus");
        if (this.antiAfkPlugin == null || !this.antiAfkPlugin.isEnabled()) {
            return;
        }

        try {
            // Get the API method from the main plugin class
            final Class<?> pluginClass = this.antiAfkPlugin.getClass();
            this.getApiMethod = pluginClass.getMethod("getAPI");

            // Get the isAfk method from the API class
            final Class<?> apiClass = this.getApiMethod.getReturnType();
            this.isAfkMethod = apiClass.getMethod("isAFK", Player.class);

            this.available = true;
        } catch (final Exception exception) {
            this.available = false;
        }
    }

}
