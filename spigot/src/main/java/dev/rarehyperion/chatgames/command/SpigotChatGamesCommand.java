package dev.rarehyperion.chatgames.command;

import dev.rarehyperion.chatgames.AbstractChatGames;
import dev.rarehyperion.chatgames.game.GameConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpigotChatGamesCommand extends ChatGamesCommand implements CommandExecutor, TabCompleter {

    public SpigotChatGamesCommand(final AbstractChatGames plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
       return this.handleCommand(sender, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            final List<String> subCommands = Arrays.asList("reload", "start", "stop", "list", "toggle", "info");

            return subCommands.stream()
                    .filter(sub -> sender.hasPermission("chatgames." + sub))
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length > 1 && args[0].equalsIgnoreCase("start")) {
            final String partial = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();

            return this.plugin.getGameRegistry().getAllConfigs().stream()
                    .map(GameConfig::getName).filter(Objects::nonNull)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

}
