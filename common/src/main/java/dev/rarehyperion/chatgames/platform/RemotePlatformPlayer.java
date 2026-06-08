package dev.rarehyperion.chatgames.platform;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public final class RemotePlatformPlayer implements PlatformPlayer {

    private final UUID id;
    private final String name;

    public RemotePlatformPlayer(final UUID id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void sendMessage(final Component component) {
        // Remote winners are represented only for messages, commands, and API events.
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public UUID id() {
        return this.id;
    }

}
