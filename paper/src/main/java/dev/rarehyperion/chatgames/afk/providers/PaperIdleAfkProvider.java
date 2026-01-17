package dev.rarehyperion.chatgames.afk.providers;

import dev.rarehyperion.chatgames.afk.AfkCheckResult;
import dev.rarehyperion.chatgames.afk.AfkProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * AFK provider using Paper's built-in idle detection API.
 *
 * <p>
 *     Uses Paper's {@code Player.isIdle()} method which tracks player
 *     activity based on movement, chat, and other interactions.
 * </p>
 *
 * @author tannerharkin
 */
public final class PaperIdleAfkProvider implements AfkProvider {

    private Method isIdleMethod;
    private boolean initialized = false;
    private boolean available = false;

    @Override
    public String id() {
        return "paper-idle";
    }

    @Override
    public String displayName() {
        return "Paper Idle API";
    }

    @Override
    public int priority() {
        return 200;
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
            final Boolean isIdle = (Boolean) this.isIdleMethod.invoke(player);
            return isIdle ? AfkCheckResult.AFK : AfkCheckResult.ACTIVE;
        } catch (final Exception exception) {
            return AfkCheckResult.UNKNOWN;
        }
    }

    private void initialize() {
        this.initialized = true;

        try {
            // Paper's isIdle() method is available on Paper servers
            this.isIdleMethod = Player.class.getMethod("isIdle");
            this.available = true;
        } catch (final NoSuchMethodException exception) {
            this.available = false;
        }
    }

}
