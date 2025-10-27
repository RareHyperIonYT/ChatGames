package me.RareHyperIon.ChatGames.games;

import me.RareHyperIon.ChatGames.ChatGames;
import me.RareHyperIon.ChatGames.handlers.LanguageHandler;
import org.bukkit.entity.Player;

import java.util.Map;

public abstract class Game {

    protected final ChatGames plugin;
    protected final GameConfig config;
    protected final LanguageHandler language;

    public Game(final ChatGames plugin, final GameConfig config, final LanguageHandler language) {
        this.plugin = plugin;
        this.config = config;
        this.language = language;
    }

    public abstract void onStart();

    public abstract void onWin(final Player player);

    public abstract void onEnd();

    public Map.Entry<String, String> getQuestion() {
        return null;
    }

}
