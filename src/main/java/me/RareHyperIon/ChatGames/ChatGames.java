package me.RareHyperIon.ChatGames;

import me.RareHyperIon.ChatGames.commands.ChatGameCommand;
import me.RareHyperIon.ChatGames.handlers.GameHandler;
import me.RareHyperIon.ChatGames.handlers.LanguageHandler;
import me.RareHyperIon.ChatGames.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class ChatGames extends JavaPlugin {

    public static Logger LOGGER = Bukkit.getLogger();

    private final LanguageHandler languageHandler;
    private final GameHandler gameHandler;

    public ChatGames() {
        this.saveDefaultConfig();

        this.languageHandler = new LanguageHandler(this);
        this.gameHandler = new GameHandler(this, this.languageHandler);
    }

    @Override
    public void onLoad() {
        this.languageHandler.load();
    }

    @Override
    public void onEnable() {
        this.gameHandler.load();

        this.getCommand("chatgames").setExecutor(new ChatGameCommand(this));
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this.gameHandler), this);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    public void reload() {
        LOGGER.info("[ChatGames] Reloading...");
        this.reloadConfig();
        this.gameHandler.reload();
        this.languageHandler.load();
    }

    public boolean logFull() {
        return Objects.equals(this.getConfig().getString("LOG_TYPE"), "FULL");
    }

}
