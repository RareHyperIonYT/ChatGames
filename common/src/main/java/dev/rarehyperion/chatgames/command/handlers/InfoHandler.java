package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.util.MessageUtil;

/**
 * Handler for the info subcommand.
 * Displays plugin information.
 *
 * @author RareHyperIon, tannerharkin
 */
public class InfoHandler implements SubCommandHandler {

    @Override
    public void execute(final CommandContext context) {
        final String authors = String.join(", and ", context.getPlugin().platform().pluginMeta().getAuthors());

        final String response =
                "<gold>ChatGames <white>•</white> <yellow>%s</yellow></gold>\n" +
                "<green>A simple plugin that adds chat-based games by <aqua>%s</aqua></green>";

        context.getSender().sendMessage(MessageUtil.parse(
                String.format(response, context.getPlugin().platform().pluginMeta().getVersion(), authors)
        ));
    }
}
