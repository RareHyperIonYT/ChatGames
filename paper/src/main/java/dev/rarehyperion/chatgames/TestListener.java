package dev.rarehyperion.chatgames;

import dev.rarehyperion.chatgames.events.ChatGameEndEvent;
import dev.rarehyperion.chatgames.events.ChatGameStartEvent;
import dev.rarehyperion.chatgames.events.ChatGameWinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TestListener implements Listener {

    @EventHandler
    public void onChatGameWin(final ChatGameWinEvent event) {
        System.out.println("WIN: " + event.getPlayer().getName());
    }

    @EventHandler
    public void onChatGameTimeout(final ChatGameEndEvent event) {
        System.out.println("END: " + event.getType() + ", REASON: " + event.getReason());
    }

    @EventHandler
    public void onChatGameStart(final ChatGameStartEvent event) {
        System.out.println("START: " + event.getType());
    }

}
