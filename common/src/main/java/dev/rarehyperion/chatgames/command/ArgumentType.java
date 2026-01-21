package dev.rarehyperion.chatgames.command;

/**
 * Defines the argument patterns for subcommands.
 *
 * @author tannerharkin
 */
public enum ArgumentType {
    /**
     * Command takes no arguments.
     */
    NONE,

    /**
     * Command takes a game name argument (can contain spaces).
     */
    GAME_NAME,

    /**
     * Command takes a single token argument (no spaces).
     */
    TOKEN
}
