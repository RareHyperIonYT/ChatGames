package dev.rarehyperion.chatgames.command;

/**
 * Functional interface for handling subcommand execution.
 *
 * @author tannerharkin
 */
@FunctionalInterface
public interface SubCommandHandler {

    /**
     * Executes the subcommand with the given context.
     *
     * @param context The command execution context.
     */
    void execute(final CommandContext context);
}
