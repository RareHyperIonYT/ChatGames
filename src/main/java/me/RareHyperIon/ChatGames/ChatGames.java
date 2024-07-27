package me.RareHyperIon.ChatGames;

import me.RareHyperIon.ChatGames.handlers.GameHandler;
import me.RareHyperIon.ChatGames.handlers.LanguageHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class ChatGames extends JavaPlugin {

    public static Logger LOGGER = Logger.getLogger("ChatGames");

    private final LanguageHandler languageHandler;
    private final GameHandler gameHandler;

    public ChatGames() {
        this.saveDefaultConfig();

        this.languageHandler = new LanguageHandler(this);
        this.gameHandler = new GameHandler(this);
    }

    @Override
    public void onLoad() {
        this.languageHandler.load();
        this.gameHandler.load();
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

}
