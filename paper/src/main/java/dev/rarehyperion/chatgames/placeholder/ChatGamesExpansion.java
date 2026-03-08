package dev.rarehyperion.chatgames.placeholder;

import dev.rarehyperion.chatgames.storage.PointsManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public final class ChatGamesExpansion extends PlaceholderExpansion {

    private final PointsManager pointsManager;

    public ChatGamesExpansion(final PointsManager pointsManager) {
        this.pointsManager = pointsManager;
    }

    @Override
    public String getIdentifier() {
        return "chatgames";
    }

    @Override
    public String getAuthor() {
        return "RareHyperIon";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(final OfflinePlayer player, final String params) {
        // %chatgames_points% - requesting player's points
        if (params.equalsIgnoreCase("points")) {
            if (player == null) return "0";
            return String.valueOf(this.pointsManager.getPoints(player.getUniqueId()));
        }

        // %chatgames_points_<name>% - specific player's points by name
        if (params.toLowerCase().startsWith("points_")) {
            final String targetName = params.substring(7);
            return String.valueOf(this.pointsManager.getPointsByName(targetName));
        }

        return null;
    }

}
