package dev.rarehyperion.chatgames;

import dev.rarehyperion.chatgames.events.ChatGameEndEvent;
import dev.rarehyperion.chatgames.events.ChatGameStartEvent;
import dev.rarehyperion.chatgames.events.ChatGameWinEvent;
import dev.rarehyperion.chatgames.platform.PlatformLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TestListener implements Listener {

    private final PlatformLogger logger;

    public TestListener(final PlatformLogger logger) {
        this.logger = logger;
    }

    @EventHandler
    public void onChatGameWin(final ChatGameWinEvent event) {
        this.logger.info("[Event] WIN: " + event.getPlayer().getName());
    }

    @EventHandler
    public void onChatGameTimeout(final ChatGameEndEvent event) {
        this.logger.info("[Event] END: " + event.getType() + ", REASON: " + event.getReason());
    }

    @EventHandler
    public void onChatGameStart(final ChatGameStartEvent event) {
        this.logger.info("[Event] START: " + event.getType());
    }

}
