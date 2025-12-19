package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.platform.PlatformPlayer;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

public class SpongePlatformPlayer implements PlatformPlayer {

    private final Player player;

    public SpongePlatformPlayer(final Player player) {
        this.player = player;
    }

    @Override
    public void sendMessage(final Component component) {
        this.player.sendMessage(component);
    }

    @Override
    public String name() {
        return this.player.name();
    }

    @Override
    public UUID id() {
        return this.player.uniqueId();
    }


}
