package dev.rarehyperion.chatgames.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.regex.Pattern;

/**
 * Utility class for parsing, formatting, and converting chat messages.
 *
 * @author RareHyperIon
 * @since 20/12/2025
 */
public final class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");

    /**
     * Parses a text string into a {@link Component}
     *
     * @param text The text to parse
     * @return The parsed Component.
     */
    public static Component parse(final String text) {
        String processed = text;

        processed = convertLegacyToMiniMessage(processed);
        return MINI_MESSAGE.deserialize(processed);

    }

    /**
     * Converts a {@link Component} to plain text.
     * @param component The Component to convert.
     * @return The plain text representation.
     */
    public static String plainText(final Component component) {
        return PLAIN_SERIALIZER.serialize(component);
    }

    /**
     * Serializes a {@link Component} into a legacy text format using § codes.
     * @param component The Component to serialize.
     * @return The legacy-formatted string.
     */
    public static String serialize(final Component component) {
        return LEGACY_SERIALIZER.serialize(component);
    }

    /**
     * Converts a legacy text string with color codes (&amp; codes and hex codes) into a MiniMessage-compatible format.
     * @param text The legacy text.
     * @return The converted MiniMessage string.
     */
    public static String convertLegacyToMiniMessage(String text) {
        text = HEX_PATTERN.matcher(text).replaceAll("<#$1>");

        return text
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");
    }

    // Not meant to be instantiated; shows clearly to anyone forking that this class isn't meant to be instantiated.
    private MessageUtil() { /* no-op */ }

}