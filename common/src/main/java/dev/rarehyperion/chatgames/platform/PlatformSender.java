package dev.rarehyperion.chatgames.platform;

import net.kyori.adventure.text.Component;

public interface PlatformSender {

    void sendMessage(final Component component);
    boolean hasPermission(final String permission);
    boolean isConsole();

}
