package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.SubCommand;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.util.MessageUtil;

/**
 * Handler for the reload subcommand.
 * Reloads the plugin configuration.
 *
 * @author RareHyperIon, tannerharkin
 */
public class ReloadHandler implements SubCommandHandler {

    private static final String NO_PERMISSION_DEFAULT = "<red>You don't have permission to use this command.</red>";

    @Override
    public void execute(final CommandContext context) {
        final String permission = SubCommand.RELOAD.getPermission();

        if (!context.hasPermission(permission)) {
            context.getSender().sendMessage(MessageUtil.parse(
                    context.getPlugin().configManager().getMessage("permission", NO_PERMISSION_DEFAULT)
            ));
            return;
        }

        context.getPlugin().reload();

        context.getSender().sendMessage(MessageUtil.parse(
                context.getPlugin().configManager().getMessage("reload", "<green>Successfully reloaded ChatGames!</green>")
        ));
    }
}
