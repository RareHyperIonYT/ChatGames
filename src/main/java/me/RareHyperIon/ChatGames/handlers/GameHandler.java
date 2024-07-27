package me.RareHyperIon.ChatGames.handlers;

import me.RareHyperIon.ChatGames.ChatGames;
import me.RareHyperIon.ChatGames.games.GameConfig;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GameHandler {

    private final ChatGames plugin;
    private final List<GameConfig> games = new ArrayList<>();

    public GameHandler(final ChatGames plugin) {
        this.plugin = plugin;
    }

    public void load() {
        final File folder = new File(this.plugin.getDataFolder(), "games");

        if(!folder.exists()) {
            // TODO: Create default chat games using jar resources.
            throw new IllegalStateException("Games folder does not exist.");
        }

        final File[] games = folder.listFiles(((dir, name) -> name.toLowerCase().endsWith(".yml")));

        if(games == null || games.length == 0) {
            ChatGames.LOGGER.warning("There are no games to load.");
            return;
        }

        for(final File file : games) {
            if(!file.isFile() || !file.getName().toLowerCase().endsWith(".yml")) continue;

            final GameConfig config = new GameConfig(YamlConfiguration.loadConfiguration(file));
            this.games.add(config);
        }
    }

}
