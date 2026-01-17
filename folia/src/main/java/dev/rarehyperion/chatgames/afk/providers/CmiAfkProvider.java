package dev.rarehyperion.chatgames.afk.providers;

import dev.rarehyperion.chatgames.afk.AfkCheckResult;
import dev.rarehyperion.chatgames.afk.AfkProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * AFK provider for CMI plugin.
 *
 * <p>
 *     Uses reflection to integrate with CMI without requiring
 *     a compile-time dependency.
 * </p>
 *
 * @author tannerharkin
 */
public final class CmiAfkProvider implements AfkProvider {

    private Plugin cmiPlugin;
    private Method getInstanceMethod;
    private Method getPlayerManagerMethod;
    private Method getUserMethod;
    private Method isAfkMethod;
    private boolean initialized = false;
    private boolean available = false;

    @Override
    public String id() {
        return "cmi";
    }

    @Override
    public String displayName() {
        return "CMI";
    }

    @Override
    public int priority() {
        return 110;
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
            // CMI.getInstance().getPlayerManager().getUser(player).isAfk()
            final Object cmiInstance = this.getInstanceMethod.invoke(null);
            if (cmiInstance == null) {
                return AfkCheckResult.UNKNOWN;
            }

            final Object playerManager = this.getPlayerManagerMethod.invoke(cmiInstance);
            if (playerManager == null) {
                return AfkCheckResult.UNKNOWN;
            }

            final Object user = this.getUserMethod.invoke(playerManager, playerId);
            if (user == null) {
                return AfkCheckResult.UNKNOWN;
            }

            final Boolean isAfk = (Boolean) this.isAfkMethod.invoke(user);
            return isAfk ? AfkCheckResult.AFK : AfkCheckResult.ACTIVE;
        } catch (final Exception exception) {
            return AfkCheckResult.UNKNOWN;
        }
    }

    private void initialize() {
        this.initialized = true;

        this.cmiPlugin = Bukkit.getPluginManager().getPlugin("CMI");
        if (this.cmiPlugin == null || !this.cmiPlugin.isEnabled()) {
            return;
        }

        try {
            // Get CMI class and getInstance method
            final Class<?> cmiClass = Class.forName("com.Zrips.CMI.CMI");
            this.getInstanceMethod = cmiClass.getMethod("getInstance");

            // Get PlayerManager from CMI
            final Object cmiInstance = this.getInstanceMethod.invoke(null);
            this.getPlayerManagerMethod = cmiClass.getMethod("getPlayerManager");

            // Get CMIUser from PlayerManager
            final Object playerManager = this.getPlayerManagerMethod.invoke(cmiInstance);
            final Class<?> playerManagerClass = playerManager.getClass();
            this.getUserMethod = playerManagerClass.getMethod("getUser", UUID.class);

            // Get isAfk from CMIUser
            final Class<?> cmiUserClass = this.getUserMethod.getReturnType();
            this.isAfkMethod = cmiUserClass.getMethod("isAfk");

            this.available = true;
        } catch (final Exception exception) {
            this.available = false;
        }
    }

}
