package dev.rarehyperion.chatgames.command;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.command.handlers.*;
import dev.rarehyperion.chatgames.platform.PlatformSender;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry for command handlers.
 * Provides handler registration, execution, and tab completion.
 *
 * @author tannerharkin
 */
public class CommandRegistry {

    private final ChatGamesCore plugin;
    private final Map<SubCommand, SubCommandHandler> handlers;

    /**
     * Creates a new command registry and registers all handlers.
     *
     * @param plugin The plugin instance.
     */
    public CommandRegistry(final ChatGamesCore plugin) {
        this.plugin = plugin;
        this.handlers = new EnumMap<>(SubCommand.class);
        this.registerHandlers();
    }

    /**
     * Registers all default command handlers.
     */
    private void registerHandlers() {
        this.handlers.put(SubCommand.RELOAD, new ReloadHandler());
        this.handlers.put(SubCommand.START, new StartHandler());
        this.handlers.put(SubCommand.STOP, new StopHandler());
        this.handlers.put(SubCommand.LIST, new ListHandler());
        this.handlers.put(SubCommand.TOGGLE, new ToggleHandler());
        this.handlers.put(SubCommand.INFO, new InfoHandler());
        this.handlers.put(SubCommand.HELP, new HelpHandler());
        this.handlers.put(SubCommand.ANSWER, new AnswerHandler());
    }

    /**
     * Returns the handler for a subcommand.
     *
     * @param subCommand The subcommand.
     * @return The handler, or null if not registered.
     */
    public SubCommandHandler getHandler(final SubCommand subCommand) {
        return this.handlers.get(subCommand);
    }

    /**
     * Executes a subcommand with the given context.
     *
     * @param subCommand The subcommand to execute.
     * @param context    The execution context.
     * @return True if the handler was found and executed.
     */
    public boolean execute(final SubCommand subCommand, final CommandContext context) {
        final SubCommandHandler handler = this.handlers.get(subCommand);
        if (handler != null) {
            handler.execute(context);
            return true;
        }
        return false;
    }

    /**
     * Gets tab completion suggestions for a subcommand.
     *
     * @param subCommand The subcommand.
     * @param sender     The command sender (for permission checks if needed).
     * @param args       The current arguments (after the subcommand name).
     * @return List of completion suggestions.
     */
    public List<String> tabComplete(final SubCommand subCommand, final PlatformSender sender, final String[] args) {
        final SubCommandHandler handler = this.handlers.get(subCommand);
        if (handler == null) {
            return Collections.emptyList();
        }

        final CommandContext context = new CommandContext(this.plugin, sender, args);
        return handler.tabComplete(context);
    }

    /**
     * Returns the plugin instance.
     *
     * @return The plugin instance.
     */
    public ChatGamesCore getPlugin() {
        return this.plugin;
    }
}
