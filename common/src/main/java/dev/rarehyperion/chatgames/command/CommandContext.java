package dev.rarehyperion.chatgames.command;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.platform.PlatformSender;

/**
 * Represents the execution context for a command.
 * Provides access to the sender, arguments, and plugin instance.
 *
 * @author tannerharkin
 */
public class CommandContext {

    private final ChatGamesCore plugin;
    private final PlatformSender sender;
    private final String[] args;

    /**
     * Creates a new command context.
     *
     * @param plugin The plugin instance.
     * @param sender The command sender.
     * @param args   The command arguments (excluding the subcommand name).
     */
    public CommandContext(final ChatGamesCore plugin, final PlatformSender sender, final String[] args) {
        this.plugin = plugin;
        this.sender = sender;
        this.args = args != null ? args : new String[0];
    }

    /**
     * Returns the plugin instance.
     *
     * @return The plugin instance.
     */
    public ChatGamesCore getPlugin() {
        return this.plugin;
    }

    /**
     * Returns the command sender.
     *
     * @return The command sender.
     */
    public PlatformSender getSender() {
        return this.sender;
    }

    /**
     * Returns the command arguments.
     *
     * @return The arguments array.
     */
    public String[] getArgs() {
        return this.args;
    }

    /**
     * Returns the argument at the specified index, or null if out of bounds.
     *
     * @param index The argument index.
     * @return The argument value, or null.
     */
    public String getArg(final int index) {
        if (index < 0 || index >= this.args.length) {
            return null;
        }
        return this.args[index];
    }

    /**
     * Returns the number of arguments.
     *
     * @return The argument count.
     */
    public int getArgCount() {
        return this.args.length;
    }

    /**
     * Checks if the sender has a permission.
     *
     * @param permission The permission to check.
     * @return True if the sender has the permission.
     */
    public boolean hasPermission(final String permission) {
        return this.sender.hasPermission(permission);
    }

    /**
     * Joins all arguments from the specified start index with spaces.
     *
     * @param startIndex The index to start joining from.
     * @return The joined string, or empty string if no arguments.
     */
    public String joinArgs(final int startIndex) {
        if (startIndex >= this.args.length) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        for (int i = startIndex; i < this.args.length; i++) {
            if (i > startIndex) {
                builder.append(" ");
            }
            builder.append(this.args[i]);
        }
        return builder.toString();
    }
}
