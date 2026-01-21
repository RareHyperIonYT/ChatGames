package dev.rarehyperion.chatgames.command;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.platform.PlatformSender;
import dev.rarehyperion.chatgames.util.MessageUtil;

import java.util.Arrays;
import java.util.Optional;

/**
 * Base command handler for ChatGames.
 * Delegates subcommand execution to registered handlers via CommandRegistry.
 *
 * @author RareHyperIon, tannerharkin
 */
public class ChatGamesCommand {

    protected final ChatGamesCore plugin;
    protected final ConfigManager configManager;
    protected final CommandRegistry registry;

    public ChatGamesCommand(final ChatGamesCore plugin, final CommandRegistry registry) {
        this.plugin = plugin;
        this.configManager = plugin.configManager();
        this.registry = registry;
    }

    /**
     * Handles command execution by delegating to the appropriate handler.
     *
     * @param sender The command sender.
     * @param args   The command arguments.
     * @return True if the command was handled.
     */
    public boolean handleCommand(final PlatformSender sender, final String[] args) {
        if (args.length == 0) {
            this.registry.execute(SubCommand.HELP, new CommandContext(this.plugin, sender, new String[0]));
            return true;
        }

        final String subCommandName = args[0].toLowerCase();
        final Optional<SubCommand> optionalSubCommand = SubCommand.fromName(subCommandName);

        if (!optionalSubCommand.isPresent()) {
            sender.sendMessage(MessageUtil.parse("<red>Unknown command. Type /chatgames for help.</red>"));
            return true;
        }

        final SubCommand subCommand = optionalSubCommand.get();
        final String[] subArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
        final CommandContext context = new CommandContext(this.plugin, sender, subArgs);

        this.registry.execute(subCommand, context);

        return true;
    }

    /**
     * Returns the command registry.
     *
     * @return The command registry.
     */
    public CommandRegistry getRegistry() {
        return this.registry;
    }
}
