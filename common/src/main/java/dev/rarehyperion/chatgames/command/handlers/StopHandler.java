package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.SubCommand;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.util.MessageUtil;

/**
 * Handler for the stop subcommand.
 * Stops the current game.
 *
 * @author RareHyperIon, tannerharkin
 */
public class StopHandler implements SubCommandHandler {

    private static final String DEFAULT_NO_PERMISSION = "<red>You don't have permission to use this command.</red>";
    private static final String DEFAULT_STOP_SUCCESS = "<green>Game stopped.</green>";

    @Override
    public void execute(final CommandContext context) {
        final ConfigManager configManager = context.getPlugin().configManager();
        final String permission = SubCommand.STOP.getPermission();

        if (!context.hasPermission(permission)) {
            context.getSender().sendMessage(MessageUtil.parse(
                    configManager.getMessage("permission", DEFAULT_NO_PERMISSION)
            ));
            return;
        }

        context.getPlugin().gameManager().stopGame();
        context.getSender().sendMessage(MessageUtil.parse(
                configManager.getMessage("stop-success", DEFAULT_STOP_SUCCESS)
        ));
    }
}
