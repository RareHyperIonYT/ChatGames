package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.game.Game;
import dev.rarehyperion.chatgames.game.types.ReactionGame;

/**
 * Handler for the answer subcommand.
 * Processes game answers for ReactionGame click events.
 * This is an internal command and is hidden from help output.
 *
 * @author RareHyperIon, tannerharkin
 */
public class AnswerHandler implements SubCommandHandler {

    // Returns null to hide from help - this is an internal command
    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public void execute(final CommandContext context) {
        final Game game = context.getPlugin().gameManager().getActiveGame();

        // Only players can answer
        if (context.getSender().isConsole()) {
            return;
        }

        // Only process for ReactionGame
        if (!(game instanceof ReactionGame)) {
            return;
        }

        // Require token argument
        if (context.getArgCount() < 1) {
            return;
        }

        context.getPlugin().gameManager().processAnswer(context.getSender().player(), context.getArg(0));
    }
}
