package dev.rarehyperion.chatgames.listener;

import dev.rarehyperion.chatgames.game.GameManager;
import dev.rarehyperion.chatgames.platform.impl.SpongePlatformPlayer;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
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

        if(this.gameManager.processAnswer(new SpongePlatformPlayer(player), message)) {
            // On the server version I was testing the plugin on, the event isn't cancellable.
            // I am on the latest API Version from Maven. Sponge is... special.
            // I've never seen something so dysfunctional, and awful to work with.
            // spongevanilla-1.21.10-17.0.0-RC2468-universal
            if(event instanceof Cancellable) {
                final Cancellable cancellable = (Cancellable) event;
                cancellable.setCancelled(true);
            }
        }
    }

}
