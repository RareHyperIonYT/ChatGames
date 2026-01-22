package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.SubCommand;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.util.MessageUtil;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handler for the start subcommand.
 * Starts a specified game.
 *
 * @author RareHyperIon, tannerharkin
 */
public class StartHandler implements SubCommandHandler {

    private static final String DEFAULT_NO_PERMISSION = "<red>You don't have permission to use this command.</red>";
    private static final String DEFAULT_USAGE = "<red>Incorrect usage. Usage: %usage%</red>";
    private static final String DEFAULT_UNKNOWN_GAME = "<red>Unknown game: %game%</red>";

    @Override
    public void execute(final CommandContext context) {
        final ConfigManager configManager = context.getPlugin().configManager();
        final String permission = SubCommand.START.getPermission();

        if (!context.hasPermission(permission)) {
            context.getSender().sendMessage(MessageUtil.parse(
                    configManager.getMessage("permission", DEFAULT_NO_PERMISSION)
            ));
            return;
        }

        if (context.getArgCount() < 1) {
            final String usage = configManager.getMessage("command-usage", DEFAULT_USAGE)
                    .replace("%usage%", SubCommand.START.getUsage());
            context.getSender().sendMessage(MessageUtil.parse(usage));
            return;
        }

        final String gameName = context.joinArgs(0);
        final Optional<GameConfig> optionalConfig = context.getPlugin().gameRegistry().getConfigByName(gameName);

        if (optionalConfig.isPresent()) {
            final GameConfig config = optionalConfig.get();
            context.getPlugin().gameManager().startGame(config);
        } else {
            final String message = configManager.getMessage("start-unknown-game", DEFAULT_UNKNOWN_GAME)
                    .replace("%game%", gameName);
            context.getSender().sendMessage(MessageUtil.parse(message));
        }
    }

    @Override
    public List<String> tabComplete(final CommandContext context) {
        // Check permission before providing suggestions
        final String permission = SubCommand.START.getPermission();
        if (permission != null && !context.hasPermission(permission)) {
            return Collections.emptyList();
        }

        final String partial = context.joinArgs(0).toLowerCase();

        return context.getPlugin().gameRegistry().getAllConfigs().stream()
                .map(GameConfig::getName)
                .filter(Objects::nonNull)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
    }
}
