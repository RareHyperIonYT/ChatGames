package me.RareHyperIon.ChatGames.utility;

import me.RareHyperIon.ChatGames.handlers.LanguageHandler;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Utility {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final boolean PLACEHOLDER_API_ENABLED =
        Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");

    public static Component colorComponent(final String string, final Player player) {
        String processed = string;

        // Apply PlaceholderAPI if available
        if (PLACEHOLDER_API_ENABLED && player != null) {
            processed = PlaceholderAPI.setPlaceholders(player, processed);
        }

        // Manually translate legacy codes to MiniMessage tags
        processed = translateLegacyToMiniMessage(processed);

        // Support MiniMessage format
        return MINI_MESSAGE.deserialize(processed);
    }

    private static String translateLegacyToMiniMessage(String legacyString) {
        // Handle hex codes: &#RRGGBB -> <#RRGGBB>
        legacyString = HEX_PATTERN.matcher(legacyString).replaceAll("<#$1>");

        // Handle standard codes
        return legacyString
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

    public static String color(final String string) {
        // First translate to MiniMessage, then deserialize and serialize to legacy
        String miniMessageString = translateLegacyToMiniMessage(string);
        return LegacyComponentSerializer.legacySection().serialize(MINI_MESSAGE.deserialize(miniMessageString));
    }

    public static Component placeholders(final String string, final Player player, final LanguageHandler language) {
        return colorComponent(string
                .replaceAll("\\{prefix}", Objects.toString(language.get("Prefix"), ""))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\n", "\n"), player);
    }

    public static String stripColor(final String string) {
        String miniMessageString = translateLegacyToMiniMessage(string);
        return PlainTextComponentSerializer.plainText().serialize(MINI_MESSAGE.deserialize(miniMessageString));
    }

    public static String format(final String string, final Object... objects) {
        StringBuilder formattedMessage = new StringBuilder(string);

        int placeholderIndex = formattedMessage.indexOf("{}");
        int objIndex = 0;
        while (placeholderIndex != -1 && objIndex < objects.length) {
            formattedMessage.replace(placeholderIndex, placeholderIndex + 2, objects[objIndex].toString());
            placeholderIndex = formattedMessage.indexOf("{}", placeholderIndex + 2);
            objIndex++;
        }

        return "&8[&6ChatGames&8] &r" + formattedMessage;
    }

}