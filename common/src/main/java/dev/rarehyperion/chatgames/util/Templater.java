package dev.rarehyperion.chatgames.util;

import dev.rarehyperion.chatgames.platform.PlatformPlayer;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class responsible for processing template strings used in ChatGames.
 * <p>
 *  Supported placeholders:
 *  <ul>
 *     <li><code>{player}</code> - replaced with the player's name</li>
 *     <li><code>{rand:min-max}</code> - replaced with a random integer between {@code min} and {@code max} (inclusive)</li>
 *  </ul>
 * </p>
 *
 * @author RareHyperIon
 * @since 20/12/2025
 */
public final class Templater {

    /**
     * Regex pattern used to match random number placeholders in the format:
     * <code>{rand:min-max}</code>
     */
    private static final Pattern RAND_PATTERN = Pattern.compile("\\{rand:(\\d+)-(\\d+)}");

    /**
     * Processes a template string by replacing supported placeholders with their computed values.
     *
     * @param text   The raw template string to process.
     * @param player The player whose data may be used during replacement.
     * @return The processed string with all placeholders resolved.
     *
     * @throws NullPointerException if {@code text} or {@code player} is {@code null}.
     */
    public static String process(final String text, final PlatformPlayer player) {
        String processed = text.replace("{player}", player.name());

        final Matcher matcher = RAND_PATTERN.matcher(processed);
        final Random random = new Random();

        final StringBuffer buffer = new StringBuffer();

        while(matcher.find()) {
            int min = Integer.parseInt(matcher.group(1));
            int max = Integer.parseInt(matcher.group(2));
            int value = random.nextInt(max - min + 1) + min;
            matcher.appendReplacement(buffer, String.valueOf(value));
        }

        matcher.appendTail(buffer);

        return buffer.toString();
    }

    // Not meant to be instantiated; shows clearly to anyone forking that this class isn't meant to be instantiated.
    private Templater() { /* no-op */ }

}
