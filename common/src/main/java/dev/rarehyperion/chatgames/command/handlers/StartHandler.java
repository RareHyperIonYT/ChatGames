package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.SubCommand;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.util.MessageUtil;

import java.util.Optional;

/**
 * Handler for the start subcommand.
 * Starts a specified game.
 *
 * @author RareHyperIon, tannerharkin
 */
public class StartHandler implements SubCommandHandler {

    private static final String NO_PERMISSION_DEFAULT = "<red>You don't have permission to use this command.</red>";
    private static final String USAGE_DEFAULT = "<red>Incorrect usage. Usage: %s</red>";

    @Override
    public void execute(final CommandContext context) {
        final String permission = SubCommand.START.getPermission();

        if (!context.hasPermission(permission)) {
            context.getSender().sendMessage(MessageUtil.parse(
                    context.getPlugin().configManager().getMessage("permission", NO_PERMISSION_DEFAULT)
            ));
            return;
        }

        if (context.getArgCount() < 1) {
            context.getSender().sendMessage(MessageUtil.parse(
                    String.format(USAGE_DEFAULT, "/chatgames start <game>")
            ));
            return;
        }

        final String gameName = context.joinArgs(0);

        final Optional<GameConfig> optionalConfig = context.getPlugin().gameRegistry().getConfigByName(gameName);

        if (optionalConfig.isPresent()) {
            final GameConfig config = optionalConfig.get();
            context.getPlugin().gameManager().startGame(config);
        } else {
            context.getSender().sendMessage(MessageUtil.parse("<red>Unknown game: " + gameName + "</red>"));
        }
    }
}
