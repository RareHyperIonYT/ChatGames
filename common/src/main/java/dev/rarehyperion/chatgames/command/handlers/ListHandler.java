package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.SubCommand;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.util.MessageUtil;

/**
 * Handler for the list subcommand.
 * Lists all available games.
 *
 * @author RareHyperIon, tannerharkin
 */
public class ListHandler implements SubCommandHandler {

    private static final String NO_PERMISSION_DEFAULT = "<red>You don't have permission to use this command.</red>";

    @Override
    public void execute(final CommandContext context) {
        final String permission = SubCommand.LIST.getPermission();

        if (!context.hasPermission(permission)) {
            context.getSender().sendMessage(MessageUtil.parse(
                    context.getPlugin().configManager().getMessage("permission", NO_PERMISSION_DEFAULT)
            ));
            return;
        }

        context.getSender().sendMessage(MessageUtil.parse("<aqua><bold>Available Games:</bold></aqua>"));

        for (final GameConfig config : context.getPlugin().gameRegistry().getAllConfigs()) {
            context.getSender().sendMessage(MessageUtil.parse("<gray>-</gray> <green>" + config.getDisplayName() + "</green>"));
        }
    }
}
