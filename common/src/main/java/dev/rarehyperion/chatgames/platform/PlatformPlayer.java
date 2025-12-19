package dev.rarehyperion.chatgames.platform;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public interface PlatformPlayer {

    void sendMessage(final Component component);

    String name();
    UUID id();

}
