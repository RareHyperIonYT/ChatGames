package me.RareHyperIon.ChatGames.utility;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class Utility {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
        LegacyComponentSerializer.legacyAmpersand();
    private static final boolean PLACEHOLDER_API_ENABLED =
        Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    public static Component colorComponent(final String string, final Player player) {
        String processed = string;

        // Apply PlaceholderAPI if available
        if (PLACEHOLDER_API_ENABLED && player != null) {
            processed = PlaceholderAPI.setPlaceholders(player, processed);
        }

        // Support legacy (&) color codes
        return LEGACY_SERIALIZER.deserialize(processed);
    }

    public static String color(final String string) {
        return LEGACY_SERIALIZER.serialize(LEGACY_SERIALIZER.deserialize(string));
    }

    public static String stripColor(final String string) {
        return LEGACY_SERIALIZER.serialize(LEGACY_SERIALIZER.deserialize(string))
            .replaceAll("§[0-9a-fk-or]", "");
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

        return "[ChatGames] " + formattedMessage;
    }

}
