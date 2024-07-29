package me.RareHyperIon.ChatGames.utility;

import org.bukkit.ChatColor;

public final class Utility {

    public static String color(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
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
