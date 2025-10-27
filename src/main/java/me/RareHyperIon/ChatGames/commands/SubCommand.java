package me.RareHyperIon.ChatGames.commands;

import org.bukkit.command.CommandSender;

public enum SubCommand {
    RELOAD("reload", "chatgames.reload"),
    START("start", "chatgames.start"),
    STOP("stop", "chatgames.stop"),
    LIST("list", "chatgames.list"),
    INFO("info", "chatgames.info"),
    TOGGLE("toggle", "chatgames.toggle");

    private final String name;
    private final String permission;

    SubCommand(final String name, final String permission) {
        this.name = name;
        this.permission = permission;
    }

    public String getName() {
        return this.name;
    }

    public String getPermission() {
        return this.permission;
    }

    public boolean hasPermission(final CommandSender sender) {
        return this.permission == null || sender.hasPermission(this.permission);
    }

    public static SubCommand fromString(final String name) {
        for (final SubCommand cmd : values()) {
            if (cmd.name.equalsIgnoreCase(name)) {
                return cmd;
            }
        }
        return null;
    }
}
