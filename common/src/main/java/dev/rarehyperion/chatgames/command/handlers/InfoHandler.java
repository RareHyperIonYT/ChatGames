package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.util.MessageUtil;

/**
 * Handler for the info subcommand.
 * Displays plugin information.
 *
 * @author RareHyperIon, tannerharkin
 */
public class InfoHandler implements SubCommandHandler {

    private static final String DEFAULT_INFO =
            "<gold>ChatGames <white>•</white> <yellow>%version%</yellow></gold>\n" +
            "<green>A simple plugin that adds chat-based games by <aqua>%authors%</aqua></green>";

    @Override
    public void execute(final CommandContext context) {
        final ConfigManager configManager = context.getPlugin().configManager();
        final String authors = String.join(", and ", context.getPlugin().platform().pluginMeta().getAuthors());
        final String version = context.getPlugin().platform().pluginMeta().getVersion();

        final String message = configManager.getMessage("info", DEFAULT_INFO)
                .replace("%version%", version)
                .replace("%authors%", authors);

        context.getSender().sendMessage(MessageUtil.parse(message));
    }
}
