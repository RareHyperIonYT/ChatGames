package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.platform.PlatformPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PaperPlatformPlayer implements PlatformPlayer {

    private final Player player;

    public PaperPlatformPlayer(final Player player) {
        this.player = player;
    }

    @Override
    public void sendMessage(final Component component) {
        this.player.sendMessage(component);
    }

    @Override
    public String name() {
        return this.player.getName();
    }

    @Override
    public UUID id() {
        return this.player.getUniqueId();
    }


}
