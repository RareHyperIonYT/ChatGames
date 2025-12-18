package dev.rarehyperion.chatgames.listener;

import dev.rarehyperion.chatgames.game.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class SpigotChatListener implements Listener {

    private final GameManager gameManager;

    public SpigotChatListener(final GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        if(this.gameManager.getActiveGame() == null) return;

        if(this.gameManager.processAnswer(event.getPlayer(), event.getMessage())) {
            event.setCancelled(true);
        }
    }

}
