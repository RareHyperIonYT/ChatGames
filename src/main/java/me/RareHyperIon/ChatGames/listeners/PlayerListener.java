package me.RareHyperIon.ChatGames.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.RareHyperIon.ChatGames.games.ActiveGame;
import me.RareHyperIon.ChatGames.handlers.GameHandler;
import me.RareHyperIon.ChatGames.utility.Utility;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;

public class PlayerListener implements Listener {

    private final GameHandler handler;

    public PlayerListener(final GameHandler handler) {
        this.handler = handler;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(final AsyncChatEvent event) {
        final ActiveGame activeGame = this.handler.getGame();
        if (activeGame == null) return;

        final Map.Entry<String, String> question = activeGame.getGame().getQuestion();
        if (question == null) return;

        // Extract plain text from the message component
        final String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        if(this.handler.attemptWin(event.getPlayer(), message)) {
            event.setCancelled(true);
        }
    }

}
