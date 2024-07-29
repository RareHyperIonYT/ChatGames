package me.RareHyperIon.ChatGames.listeners;

import me.RareHyperIon.ChatGames.games.ActiveGame;
import me.RareHyperIon.ChatGames.handlers.GameHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;


public class PlayerListener implements Listener {

    private final GameHandler handler;

    public PlayerListener(final GameHandler handler) {
        this.handler = handler;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        final ActiveGame game = this.handler.getGame();
        if(game == null) return;

        final String message = event.getMessage();

        if(message.equalsIgnoreCase(game.question.getValue())) {
            this.handler.win(event.getPlayer());
        }
    }

}
