package me.RareHyperIon.ChatGames.handlers;

import me.RareHyperIon.ChatGames.ChatGames;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LanguageHandler {

    private final ChatGames plugin;

    private Map<String, String> translations = new HashMap<>();

    public LanguageHandler(final ChatGames plugin) {
        this.plugin = plugin;
    }

    public void load() {
        final String language = this.plugin.getConfig().getString("Language", "EN-US");

        final File file = new File(plugin.getDataFolder(), "language/" + language + ".yml");

        if(!file.exists()) {
            throw new IllegalStateException("The language \"" + language + "\" doesn't have any translations.");
        }

        final FileConfiguration lang = YamlConfiguration.loadConfiguration(file);

        for(final String key : lang.getKeys(false)) {
            this.translations.put(key, lang.getString(key));
        }
    }

}
