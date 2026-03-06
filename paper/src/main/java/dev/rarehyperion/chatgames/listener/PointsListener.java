package dev.rarehyperion.chatgames.listener;

import dev.rarehyperion.chatgames.events.ChatGameWinEvent;
import dev.rarehyperion.chatgames.storage.PointsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PointsListener implements Listener {

    private final PointsManager pointsManager;

    public PointsListener(final PointsManager pointsManager) {
        this.pointsManager = pointsManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWin(final ChatGameWinEvent event) {
        final Player player = event.getPlayer();
        this.pointsManager.addWin(player.getUniqueId(), player.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent event) {
        this.pointsManager.loadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(final PlayerQuitEvent event) {
        this.pointsManager.unloadPlayer(event.getPlayer().getUniqueId());
    }

}
