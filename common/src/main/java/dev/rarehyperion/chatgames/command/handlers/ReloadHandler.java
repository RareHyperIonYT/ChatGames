package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.SubCommand;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.util.MessageUtil;

/**
 * Handler for the reload subcommand.
 * Reloads the plugin configuration.
 *
 * @author RareHyperIon, tannerharkin
 */
public class ReloadHandler implements SubCommandHandler {

    private static final String DEFAULT_NO_PERMISSION = "<red>You don't have permission to use this command.</red>";
    private static final String DEFAULT_RELOAD = "<green>Successfully reloaded ChatGames!</green>";

    @Override
    public void execute(final CommandContext context) {
        final ConfigManager configManager = context.getPlugin().configManager();
        final String permission = SubCommand.RELOAD.getPermission();

        if (!context.hasPermission(permission)) {
            context.getSender().sendMessage(MessageUtil.parse(
                    configManager.getMessage("permission", DEFAULT_NO_PERMISSION)
            ));
            return;
        }

        context.getPlugin().reload();

        context.getSender().sendMessage(MessageUtil.parse(
                configManager.getMessage("reload", DEFAULT_RELOAD)
        ));
    }
}
