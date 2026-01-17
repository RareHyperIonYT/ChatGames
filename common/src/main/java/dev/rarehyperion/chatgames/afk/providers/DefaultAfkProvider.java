package dev.rarehyperion.chatgames.afk.providers;

import dev.rarehyperion.chatgames.afk.AfkCheckResult;
import dev.rarehyperion.chatgames.afk.AfkProvider;

import java.util.UUID;

/**
 * Default AFK provider that always returns ACTIVE.
 *
 * <p>
 *     This provider serves as the final fallback in the provider chain.
 *     It always returns ACTIVE, treating all players as not AFK.
 * </p>
 *
 * @author tannerharkin
 */
public final class DefaultAfkProvider implements AfkProvider {

    @Override
    public String id() {
        return "default";
    }

    @Override
    public String displayName() {
        return "Default (No AFK Detection)";
    }

    @Override
    public int priority() {
        return 1000;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public AfkCheckResult checkAfk(final UUID playerId) {
        return AfkCheckResult.ACTIVE;
    }

}
