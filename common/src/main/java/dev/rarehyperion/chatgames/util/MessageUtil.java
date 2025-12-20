package dev.rarehyperion.chatgames.util;

import dev.rarehyperion.chatgames.platform.PlatformPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

//    private static final boolean PLACEHOLDER_API_ENABLED = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern RAND_PATTERN = Pattern.compile("\\{rand:(\\d+)-(\\d+)}");

    private MessageUtil() { /* no-op */ }

    public static Component parse(final String text) {
        return parse(text, null);
    }

    public static Component parse(final String text, final PlatformPlayer player) {
        String processed = text;

//        if(PLACEHOLDER_API_ENABLED && player != null) {
//            processed = PlaceholderAPI.setPlaceholders(player, processed);
//        }

        processed = convertLegacyToMiniMessage(processed);

        return MINI_MESSAGE.deserialize(processed);
    }

    public static String process(final String text, final PlatformPlayer player) {
        String processed = text.replace("{player}", player.name());

        final Matcher matcher = RAND_PATTERN.matcher(processed);
        final Random random = new Random();

        final StringBuilder buffer = new StringBuilder();

        while(matcher.find()) {
            int min = Integer.parseInt(matcher.group(1));
            int max = Integer.parseInt(matcher.group(2));
            int value = random.nextInt(max - min + 1) + min;
            matcher.appendReplacement(buffer, String.valueOf(value));
        }

        matcher.appendTail(buffer);

        return buffer.toString();
    }

    public static String plainText(final Component component) {
        return PLAIN_SERIALIZER.serialize(component);
    }

    public static String serialize(final Component component) {
        return LEGACY_SERIALIZER.serialize(component);
    }

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

    public static String stripColors(final String text) {
        final String miniMessage = convertLegacyToMiniMessage(text);
        return PLAIN_SERIALIZER.serialize(MINI_MESSAGE.deserialize(miniMessage));
    }

    public static String format(final String template, Object... args) {
        final StringBuilder result = new StringBuilder(template);
        int placeholderIndex = result.indexOf("{}");
        int argIndex = 0;

        while (placeholderIndex != -1 && argIndex < args.length) {
            result.replace(placeholderIndex, placeholderIndex + 2, args[argIndex].toString());
            placeholderIndex = result.indexOf("{}", placeholderIndex);
            argIndex++;
        }

        return result.toString();
    }


}