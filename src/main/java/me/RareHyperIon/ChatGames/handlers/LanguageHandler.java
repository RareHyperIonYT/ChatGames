package me.RareHyperIon.ChatGames.handlers;

import me.RareHyperIon.ChatGames.ChatGames;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageHandler {

    private final ChatGames plugin;

    private final Map<String, String> translations = new HashMap<>();

    public LanguageHandler(final ChatGames plugin) {
        this.plugin = plugin;
    }

    public final void load() {
        this.translations.clear();

        final String language = this.plugin.getConfig().getString("Language", "EN-US");

        if(!(new File(this.plugin.getDataFolder(), "language")).exists()) {
            this.saveDefault();
        }

        final File file = new File(plugin.getDataFolder(), "language/" + language + ".yml");

        if(!file.exists()) {
            this.saveDefault();

            if(!file.exists()) {
                throw new IllegalStateException("The language \"" + language + "\" doesn't have any translations.");
            }
        }

        final FileConfiguration lang = YamlConfiguration.loadConfiguration(file);

        for(final String key : lang.getKeys(false)) {
            this.translations.put(key, lang.getString(key));
        }

        this.plugin.getSLF4JLogger().info("Loaded language: {}", language);
    }

    public final String get(final String key) {
        return this.translations.get(key);
    }

    private void saveDefault() {
        final File folder = new File(this.plugin.getDataFolder(), "language");
        if(!folder.exists() && !folder.mkdirs()) throw new IllegalStateException("Failed to create language folder.");

        for(final String language : List.of("EN-US.yml")) {
            final File out = new File(folder, language);

            try(final InputStream stream = this.plugin.getResource("language/" + language)) {
                if(stream == null) throw new IllegalStateException("Resource not found in jar.");
                Files.copy(stream, out.toPath());
            } catch (final IOException e) {
                throw new IllegalStateException("Failed to create default language file: " + language, e);
            }
        }

        this.plugin.getSLF4JLogger().info("Created default language configurations.");
    }

}
