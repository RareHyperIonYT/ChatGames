package me.RareHyperIon.ChatGames.utility;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class Utility {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final boolean PLACEHOLDER_API_ENABLED =
        Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    public static Component colorComponent(final String string, final Player player) {
        String processed = string;

        // Apply PlaceholderAPI if available
        if (PLACEHOLDER_API_ENABLED && player != null) {
            processed = PlaceholderAPI.setPlaceholders(player, processed);
        }

        // Support MiniMessage format
        return MINI_MESSAGE.deserialize(processed);
    }

    public static String color(final String string) {
        // Convert MiniMessage to legacy for compatibility
        return LegacyComponentSerializer.legacySection().serialize(MINI_MESSAGE.deserialize(string));
    }

    public static String stripColor(final String string) {
        return MINI_MESSAGE.stripTags(string);
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
