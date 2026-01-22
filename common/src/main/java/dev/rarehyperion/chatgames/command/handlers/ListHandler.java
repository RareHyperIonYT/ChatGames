package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.SubCommand;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.util.MessageUtil;

/**
 * Handler for the list subcommand.
 * Lists all available games.
 *
 * @author RareHyperIon, tannerharkin
 */
public class ListHandler implements SubCommandHandler {

    private static final String DEFAULT_NO_PERMISSION = "<red>You don't have permission to use this command.</red>";
    private static final String DEFAULT_HEADER = "<aqua><bold>Available Games:</bold></aqua>";
    private static final String DEFAULT_ENTRY = "<gray>-</gray> <green>%game%</green>";

    @Override
    public void execute(final CommandContext context) {
        final ConfigManager configManager = context.getPlugin().configManager();
        final String permission = SubCommand.LIST.getPermission();

        if (!context.hasPermission(permission)) {
            context.getSender().sendMessage(MessageUtil.parse(
                    configManager.getMessage("permission", DEFAULT_NO_PERMISSION)
            ));
            return;
        }

        // Send header
        context.getSender().sendMessage(MessageUtil.parse(
                configManager.getMessage("list-header", DEFAULT_HEADER)
        ));

        // Send each game entry
        final String entryTemplate = configManager.getMessage("list-entry", DEFAULT_ENTRY);
        for (final GameConfig config : context.getPlugin().gameRegistry().getAllConfigs()) {
            final String entry = entryTemplate.replace("%game%", config.getDisplayName());
            context.getSender().sendMessage(MessageUtil.parse(entry));
        }
    }
}
