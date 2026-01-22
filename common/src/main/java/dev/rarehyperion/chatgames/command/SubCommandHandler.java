package dev.rarehyperion.chatgames.command;

import java.util.Collections;
import java.util.List;

/**
 * Interface for handling subcommand execution and tab completion.
 * Handlers are the primary source of command behavior - they define
 * permissions, usage strings, descriptions, tab completion, and execution logic.
 *
 * @author tannerharkin
 */
public interface SubCommandHandler {

    /**
     * Executes the subcommand with the given context.
     *
     * @param context The command execution context.
     */
    void execute(CommandContext context);

    /**
     * Returns the permission required to use this command, or null if no permission is needed.
     * This is the single source of truth for permission checks.
     *
     * @return The permission string, or null if no permission required.
     */
    default String getPermission() {
        return null;
    }

    /**
     * Returns the usage string for this command (e.g., "/chatgames start <game>").
     * Return null to hide this command from help output.
     *
     * @return The usage string, or null if this command should be hidden from help.
     */
    default String getUsage() {
        return null;
    }

    /**
     * Returns the description of what this command does.
     * Only used if {@link #getUsage()} returns non-null.
     *
     * @return The command description.
     */
    default String getDescription() {
        return "";
    }

    /**
     * Provides tab completion suggestions for this subcommand.
     * Override this method in handlers that need custom completions.
     *
     * <p><b>Note:</b> Implementations should check permissions before
     * returning suggestions to prevent information leakage.</p>
     *
     * @param context The command context containing args and plugin access.
     * @return List of completion suggestions, or empty list if none.
     */
    default List<String> tabComplete(final CommandContext context) {
        return Collections.emptyList();
    }

    /**
     * Checks if the sender has permission to use this command.
     * Convenience method that checks against {@link #getPermission()}.
     *
     * @param context The command context.
     * @return True if the sender has permission or no permission is required.
     */
    default boolean hasPermission(final CommandContext context) {
        final String permission = getPermission();
        return permission == null || context.hasPermission(permission);
    }
}
