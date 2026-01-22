package dev.rarehyperion.chatgames.command;

import java.util.Collections;
import java.util.List;

/**
 * Interface for handling subcommand execution and tab completion.
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
     * Provides tab completion suggestions for this subcommand.
     * Override this method in handlers that need custom completions.
     *
     * <p><b>Important:</b> Implementations should check permissions before
     * returning suggestions to prevent information leakage to unauthorized users.</p>
     *
     * @param context The command context containing args and plugin access.
     * @return List of completion suggestions, or empty list if none/not permitted.
     */
    default List<String> tabComplete(final CommandContext context) {
        return Collections.emptyList();
    }
}
