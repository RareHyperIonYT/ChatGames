package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.util.MessageUtil;

/**
 * Handler for the help subcommand.
 * Displays command help.
 *
 * @author RareHyperIon, tannerharkin
 */
public class HelpHandler implements SubCommandHandler {

    @Override
    public void execute(final CommandContext context) {
        context.getSender().sendMessage(MessageUtil.parse("<gray><bold>ChatGames Commands:</bold></gray>"));
        context.getSender().sendMessage(MessageUtil.parse("<yellow>/chatgames reload</yellow> <gray>- Reloads the plugin</gray>"));
        context.getSender().sendMessage(MessageUtil.parse("<yellow>/chatgames start <game></yellow> <gray>- Starts the specified game</gray>"));
        context.getSender().sendMessage(MessageUtil.parse("<yellow>/chatgames stop</yellow> <gray>- Stop the current game</gray>"));
        context.getSender().sendMessage(MessageUtil.parse("<yellow>/chatgames list</yellow> <gray>- Lists all available games</gray>"));
        context.getSender().sendMessage(MessageUtil.parse("<yellow>/chatgames toggle</yellow> <gray>- Toggles automatic games</gray>"));
        context.getSender().sendMessage(MessageUtil.parse("<yellow>/chatgames info</yellow> <gray>- Displays plugin information</gray>"));
    }
}
