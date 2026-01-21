package dev.rarehyperion.chatgames.command;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.platform.PlatformSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spigot-specific command implementation using CommandExecutor and TabCompleter.
 * Uses SubCommand enum for tab completion suggestions.
 *
 * @author RareHyperIon, tannerharkin
 */
public class SpigotChatGamesCommand extends ChatGamesCommand implements CommandExecutor, TabCompleter {

    public SpigotChatGamesCommand(final ChatGamesCore plugin, final CommandRegistry registry) {
        super(plugin, registry);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final PlatformSender sender = this.plugin.platform().wrapSender(commandSender);
        return this.handleCommand(sender, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            // Filter visible subcommands by permission and prefix
            return SubCommand.getVisibleCommands().stream()
                    .filter(cmd -> !cmd.requiresPermission() || sender.hasPermission(cmd.getPermission()))
                    .map(SubCommand::getName)
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Handle second-level arguments for "start" command
        if (args.length > 1 && args[0].equalsIgnoreCase("start")) {
            final SubCommand startCmd = SubCommand.START;
            if (!startCmd.requiresPermission() || sender.hasPermission(startCmd.getPermission())) {
                final String partial = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
                return this.registry.getGameNamesStartingWith(partial);
            }
        }

        return new ArrayList<>();
    }
}
