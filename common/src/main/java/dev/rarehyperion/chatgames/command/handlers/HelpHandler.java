package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.SubCommand;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.util.MessageUtil;

/**
 * Handler for the help subcommand.
 * Dynamically displays command help based on SubCommand enum.
 *
 * @author RareHyperIon, tannerharkin
 */
public class HelpHandler implements SubCommandHandler {

    private static final String DEFAULT_HEADER = "<gray><bold>ChatGames Commands:</bold></gray>";
    private static final String DEFAULT_ENTRY = "<yellow>%usage%</yellow> <gray>- %description%</gray>";

    @Override
    public void execute(final CommandContext context) {
        final ConfigManager configManager = context.getPlugin().configManager();

        // Send header
        final String header = configManager.getMessage("help-header", DEFAULT_HEADER);
        context.getSender().sendMessage(MessageUtil.parse(header));

        // Send each visible command
        final String entryTemplate = configManager.getMessage("help-entry", DEFAULT_ENTRY);
        for (final SubCommand cmd : SubCommand.getVisibleCommands()) {
            final String entry = entryTemplate
                    .replace("%usage%", cmd.getUsage())
                    .replace("%description%", cmd.getDescription());
            context.getSender().sendMessage(MessageUtil.parse(entry));
        }
    }
}
