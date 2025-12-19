package dev.rarehyperion.chatgames.listener;

import dev.rarehyperion.chatgames.game.GameManager;
import dev.rarehyperion.chatgames.util.MessageUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@SuppressWarnings("ClassCanBeRecord")
public class PaperChatListener implements Listener {

    private final GameManager gameManager;

    public PaperChatListener(final GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(final AsyncChatEvent event) {
        if (this.gameManager.getActiveGame() == null) return;

        final String message = MessageUtil.plainText(event.message());

        if (this.gameManager.processAnswer(event.getPlayer().getUniqueId(), message)) {
            event.setCancelled(true);
        }
    }

}
