package dev.rarehyperion.chatgames.listener;

import dev.rarehyperion.chatgames.game.GameManager;
import dev.rarehyperion.chatgames.util.MessageUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.PlayerChatEvent;

public class SpongeChatListener {

    private final GameManager gameManager;

    public SpongeChatListener(final GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Listener
    public void onPlayerChat(final PlayerChatEvent event, @First Player player) {
        if(this.gameManager.getActiveGame() == null) return;

        final String message = MessageUtil.plainText(event.message());

        if(this.gameManager.processAnswer(player.uniqueId(), message)) {
            event.setCancelled(true);
        }
    }

}
