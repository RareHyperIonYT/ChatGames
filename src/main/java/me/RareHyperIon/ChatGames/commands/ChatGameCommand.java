package me.RareHyperIon.ChatGames.commands;

import me.RareHyperIon.ChatGames.ChatGames;
import me.RareHyperIon.ChatGames.games.GameConfig;
import me.RareHyperIon.ChatGames.handlers.GameHandler;
import me.RareHyperIon.ChatGames.utility.Utility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ChatGameCommand implements CommandExecutor, TabCompleter {

    private final ChatGames plugin;

    public ChatGameCommand(final ChatGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command cmd,
                             @NotNull final String label, @NotNull final String[] args) {

        final String prefix = this.plugin.getLanguageHandler().get("Prefix");
        final Player player = sender instanceof Player ? (Player) sender : null;

        if (args.length == 0) {
            sender.sendMessage(Utility.colorComponent(prefix + " &cUsage: /cg <subcommand>", player));
            return true;
        }

        final SubCommand subCommand = SubCommand.fromString(args[0]);

        if (subCommand == null) {
            sender.sendMessage(Utility.colorComponent(prefix + " &cUnknown subcommand. Usage: /cg <subcommand>", player));
            return true;
        }

        if (!subCommand.hasPermission(sender)) {
            sender.sendMessage(Utility.colorComponent(prefix + " &cYou don't have permission to use this command.", player));
            return true;
        }

        final GameHandler gameHandler = this.plugin.getGameHandler();

        switch (subCommand) {
            case RELOAD -> {
                this.plugin.reload();
                sender.sendMessage(Utility.colorComponent(prefix + " &aSuccessfully reloaded ChatGames.", player));
            }
            case START -> {
                if (args.length < 2) {
                    sender.sendMessage(Utility.colorComponent(prefix + " &cUsage: /cg start <game>", player));
                    return true;
                }
                // Join all arguments after "start" to support multi-word game names
                final String gameName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                final GameConfig gameConfig = gameHandler.getGames().stream()
                    .filter(game -> game.name.equalsIgnoreCase(gameName))
                    .findFirst()
                    .orElse(null);
                if (gameConfig == null) {
                    sender.sendMessage(Utility.colorComponent(prefix + " &cUnknown game: " + gameName, player));
                    return true;
                }
                gameHandler.startGame(gameConfig);
                // Message is sent by the game's onStart() method, no need to send here
            }
            case STOP -> {
                gameHandler.stopGame();
                sender.sendMessage(Utility.colorComponent(prefix + " &aGame stopped.", player));
            }
            case LIST -> {
                sender.sendMessage(Utility.colorComponent(prefix + " &b&lAvailable games:", player));
                for (final GameConfig game : gameHandler.getGames()) {
                    sender.sendMessage(Utility.colorComponent("&e- &a" + game.displayName, player));
                }
            }
            case INFO -> sender.sendMessage(Utility.colorComponent(prefix + " &aChatGames v" + this.plugin.getDescription().getVersion(), player));
            case TOGGLE -> {
                final boolean automaticGames = !this.plugin.getConfig().getBoolean("AutomaticGames");
                this.plugin.getConfig().set("AutomaticGames", automaticGames);
                this.plugin.saveConfig();
                gameHandler.setAutomaticGames(automaticGames);
                sender.sendMessage(Utility.colorComponent(prefix + " &aAutomatic games have been " + (automaticGames ? "enabled" : "disabled") + ".", player));
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command cmd,
                                                 @NotNull final String label, @NotNull final String[] args) {
        if (args.length == 1) {
            return Stream.of(SubCommand.values())
                .filter(subCmd -> subCmd.hasPermission(sender))
                .map(SubCommand::getName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList();
        }
        if (args.length > 1 && args[0].equalsIgnoreCase("start")) {
            final String partialGameName = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
            return this.plugin.getGameHandler().getGames().stream()
                .map(game -> game.name)
                .filter(name -> name.toLowerCase().startsWith(partialGameName))
                .toList();
        }
        return List.of();
    }

}