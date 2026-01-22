package dev.rarehyperion.chatgames.command;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Registry of subcommand identifiers.
 * Handlers define all behavior - this enum only identifies commands by name.
 *
 * @author RareHyperIon, tannerharkin
 */
public enum SubCommand {
    RELOAD("reload"),
    START("start"),
    STOP("stop"),
    LIST("list"),
    TOGGLE("toggle"),
    INFO("info"),
    HELP("help"),
    ANSWER("answer");

    private final String name;

    SubCommand(final String name) {
        this.name = name;
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
     * Returns all subcommand names.
     *
     * @return List of command names.
     */
    public static List<String> getAllNames() {
        return Arrays.stream(values())
                .map(SubCommand::getName)
                .collect(Collectors.toList());
    }
}
