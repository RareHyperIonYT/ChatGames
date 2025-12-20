package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.platform.PlatformPlayer;
import dev.rarehyperion.chatgames.platform.PlatformSender;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class SpongePlatformSender implements PlatformSender {

    private final Object source; // ServerPlayer, SystemSubject, Audience, etc.

    public SpongePlatformSender(final Object source) {
        this.source = source;
    }

    @Override
    public void sendMessage(final Component component) {
        if(this.source instanceof ServerPlayer) {
            final ServerPlayer serverPlayer = (ServerPlayer) this.source;
            serverPlayer.sendMessage(component);
        } else if(this.source instanceof SystemSubject) {
            final SystemSubject systemSubject = (SystemSubject) this.source;
            systemSubject.sendMessage(component);
        } else if(this.source instanceof Audience) {
            final Audience audience = (Audience) this.source;
            audience.sendMessage(component);
        } else {
            throw new IllegalArgumentException("Invalid argument passed: " + this.source);
        }
    }

    @Override
    public boolean hasPermission(final String permission) {
        if(this.source instanceof ServerPlayer) {
            final ServerPlayer serverPlayer = (ServerPlayer) this.source;
            return serverPlayer.hasPermission(permission);
        } else if(this.source instanceof SystemSubject) {
            final SystemSubject systemSubject = (SystemSubject) this.source;
            return systemSubject.hasPermission(permission);
        } else {
            throw new IllegalArgumentException("Invalid argument passed: " + this.source);
        }
    }

    @Override
    public boolean isConsole() {
        return this.source instanceof SystemSubject;
    }

    @Override
    public PlatformPlayer player() {
        if(this.source instanceof ServerPlayer) {
            final ServerPlayer serverPlayer = (ServerPlayer) this.source;
            return new SpongePlatformPlayer(serverPlayer);
        }

        throw new IllegalStateException("Sender is not a player");
    }

    public Object unwrap() {
        return this.source;
    }

}
