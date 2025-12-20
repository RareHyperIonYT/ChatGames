package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.platform.PlatformSender;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

@SuppressWarnings("ClassCanBeRecord")
public class FoliaPlatformSender implements PlatformSender {

    private final CommandSender sender;

    public FoliaPlatformSender(final CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendMessage(final Component component) {
        this.sender.sendMessage(component);
    }

    @Override
    public boolean hasPermission(final String permission) {
        return this.sender.hasPermission(permission);
    }

    @Override
    public boolean isConsole() {
        return this.sender instanceof ConsoleCommandSender;
    }

    // Will probably forever remain unused for this project.
    public CommandSender unwrap() {
        return this.sender;
    }

}
