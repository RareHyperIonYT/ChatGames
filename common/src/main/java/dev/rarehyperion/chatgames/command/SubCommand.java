package dev.rarehyperion.chatgames.command;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Single source of truth for all command metadata.
 * Defines command names, permissions, descriptions, and argument types.
 *
 * @author RareHyperIon, tannerharkin
 */
public enum SubCommand {
    RELOAD("reload", "chatgames.reload", "Reloads configuration", ArgumentType.NONE),
    START("start", "chatgames.start", "Starts a game", ArgumentType.GAME_NAME),
    STOP("stop", "chatgames.stop", "Stops current game", ArgumentType.NONE),
    LIST("list", "chatgames.list", "Lists available games", ArgumentType.NONE),
    TOGGLE("toggle", "chatgames.toggle", "Toggles automatic games", ArgumentType.NONE),
    INFO("info", null, "Plugin information", ArgumentType.NONE),
    HELP("help", null, "Shows help", ArgumentType.NONE),
    ANSWER("answer", null, "Submit answer", ArgumentType.TOKEN);

    private final String name;
    private final String permission;
    private final String description;
    private final ArgumentType argumentType;

    SubCommand(final String name, final String permission, final String description, final ArgumentType argumentType) {
        this.name = name;
        this.permission = permission;
        this.description = description;
        this.argumentType = argumentType;
    }

    /**
     * Returns the command name (lowercase).
     *
     * @return The command name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the permission node required for this command, or null if no permission is required.
     *
     * @return The permission node, or null.
     */
    public String getPermission() {
        return this.permission;
    }

    /**
     * Returns the description of what this command does.
     *
     * @return The command description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the argument type expected by this command.
     *
     * @return The argument type.
     */
    public ArgumentType getArgumentType() {
        return this.argumentType;
    }

    /**
     * Checks if this command requires a permission.
     *
     * @return True if permission is required.
     */
    public boolean requiresPermission() {
        return this.permission != null;
    }

    /**
     * Finds a subcommand by name (case-insensitive).
     *
     * @param name The command name to find.
     * @return An Optional containing the subcommand if found.
     */
    public static Optional<SubCommand> fromName(final String name) {
        if (name == null) {
            return Optional.empty();
        }
        final String lowerName = name.toLowerCase();
        return Arrays.stream(values())
                .filter(cmd -> cmd.name.equals(lowerName))
                .findFirst();
    }

    /**
     * Returns all subcommands that should be visible in help and tab completion.
     * Excludes internal commands like ANSWER.
     *
     * @return List of visible subcommands.
     */
    public static List<SubCommand> getVisibleCommands() {
        return Arrays.stream(values())
                .filter(cmd -> cmd != ANSWER)
                .collect(Collectors.toList());
    }

    /**
     * Returns all subcommand names.
     *
     * @return List of command names.
     */
    public static List<String> getAllNames() {
        return Arrays.stream(values())
                .map(SubCommand::getName)
                .collect(Collectors.toList());
    }

    /**
     * Returns all visible subcommand names (for tab completion).
     *
     * @return List of visible command names.
     */
    public static List<String> getVisibleNames() {
        return getVisibleCommands().stream()
                .map(SubCommand::getName)
                .collect(Collectors.toList());
    }
}
