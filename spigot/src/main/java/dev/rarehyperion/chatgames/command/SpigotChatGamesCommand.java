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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Spigot-specific command implementation using CommandExecutor and TabCompleter.
 * Delegates tab completion and permissions to handlers via CommandRegistry.
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
            // Complete subcommand names, filtered by permission from handlers
            final List<String> suggestions = new ArrayList<>();
            final String partial = args[0].toLowerCase();

            for (final SubCommand subCommand : SubCommand.values()) {
                final SubCommandHandler handler = this.registry.getHandler(subCommand);
                if (handler == null) {
                    continue;
                }

                // Skip commands with null usage (hidden commands)
                if (handler.getUsage() == null) {
                    continue;
                }

                // Check permission from handler
                final String permission = handler.getPermission();
                if (permission != null && !sender.hasPermission(permission)) {
                    continue;
                }

                final String name = subCommand.getName();
                if (name.startsWith(partial)) {
                    suggestions.add(name);
                }
            }

            return suggestions;
        }

        if (args.length > 1) {
            // Delegate to handler's tab completion
            final Optional<SubCommand> optionalCmd = SubCommand.fromName(args[0]);
            if (optionalCmd.isPresent()) {
                final SubCommand subCommand = optionalCmd.get();
                final PlatformSender platformSender = this.plugin.platform().wrapSender(sender);
                final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return this.registry.tabComplete(subCommand, platformSender, subArgs);
            }
        }

        return Collections.emptyList();
    }
}
