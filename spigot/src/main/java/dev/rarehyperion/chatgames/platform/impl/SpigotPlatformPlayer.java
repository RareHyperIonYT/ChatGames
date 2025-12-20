package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.platform.PlatformPlayer;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SpigotPlatformPlayer implements PlatformPlayer {

    private final Player player;

    public SpigotPlatformPlayer(final Player player) {
        this.player = player;
    }

    @Override
    public void sendMessage(final Component component) {
        final String legacy = MessageUtil.serialize(component);
        this.player.sendMessage(legacy);
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
