package dev.rarehyperion.chatgames.command;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.platform.PlatformSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Spigot-specific command implementation using CommandExecutor and TabCompleter.
 * Delegates tab completion to handlers via CommandRegistry.
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
            // Complete subcommand names, filtered by permission
            return SubCommand.getVisibleCommands().stream()
                    .filter(cmd -> !cmd.requiresPermission() || sender.hasPermission(cmd.getPermission()))
                    .map(SubCommand::getName)
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length > 1) {
            // Delegate to handler's tab completion
            final Optional<SubCommand> optionalCmd = SubCommand.fromName(args[0]);
            if (optionalCmd.isPresent()) {
                final SubCommand subCommand = optionalCmd.get();

                // Check permission before offering completions
                if (subCommand.requiresPermission() && !sender.hasPermission(subCommand.getPermission())) {
                    return Collections.emptyList();
                }

                final PlatformSender platformSender = this.plugin.platform().wrapSender(sender);
                final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return this.registry.tabComplete(subCommand, platformSender, subArgs);
            }
        }

        return Collections.emptyList();
    }
}
