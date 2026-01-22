package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.CommandRegistry;
import dev.rarehyperion.chatgames.command.SubCommand;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.util.MessageUtil;

import java.util.Map;

/**
 * Handler for the help subcommand.
 * Dynamically displays command help by querying registered handlers.
 *
 * @author RareHyperIon, tannerharkin
 */
public class HelpHandler implements SubCommandHandler {

    private static final String DEFAULT_HEADER = "<gray><bold>ChatGames Commands:</bold></gray>";
    private static final String DEFAULT_ENTRY = "<yellow>%usage%</yellow> <gray>- %description%</gray>";

    @Override
    public String getUsage() {
        return "/chatgames help";
    }

    @Override
    public String getDescription() {
        return "Shows this help message";
    }

    @Override
    public void execute(final CommandContext context) {
        final ConfigManager configManager = context.getPlugin().configManager();
        final CommandRegistry registry = context.getPlugin().commandRegistry();

        // Send header
        final String header = configManager.getMessage("help-header", DEFAULT_HEADER);
        context.getSender().sendMessage(MessageUtil.parse(header));

        // Send each visible command (those with non-null usage)
        final String entryTemplate = configManager.getMessage("help-entry", DEFAULT_ENTRY);
        for (final Map.Entry<SubCommand, SubCommandHandler> entry : registry.getHandlers().entrySet()) {
            final SubCommandHandler handler = entry.getValue();
            final String usage = handler.getUsage();

            // Skip commands that return null for usage (hidden commands)
            if (usage == null) {
                continue;
            }

            final String line = entryTemplate
                    .replace("%usage%", usage)
                    .replace("%description%", handler.getDescription());
            context.getSender().sendMessage(MessageUtil.parse(line));
        }
    }
}
