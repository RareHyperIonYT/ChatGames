package me.RareHyperIon.ChatGames;

import me.RareHyperIon.ChatGames.commands.ChatGameCommand;
import me.RareHyperIon.ChatGames.commands.InternalCommand;
import me.RareHyperIon.ChatGames.handlers.GameHandler;
import me.RareHyperIon.ChatGames.handlers.LanguageHandler;
import me.RareHyperIon.ChatGames.listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ChatGames extends JavaPlugin {

    private LanguageHandler languageHandler;
    private GameHandler gameHandler;

    @Override
    public void onLoad() {
        this.saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        // Initialize handlers
        this.languageHandler = new LanguageHandler(this);
        this.gameHandler = new GameHandler(this, this.languageHandler);

        // Load language and games
        this.languageHandler.load();
        this.gameHandler.load();

        // Register commands
        final ChatGameCommand commandExecutor = new ChatGameCommand(this);
        Objects.requireNonNull(this.getCommand("chatgames")).setExecutor(commandExecutor);
        Objects.requireNonNull(this.getCommand("chatgames")).setTabCompleter(commandExecutor);
        Objects.requireNonNull(this.getCommand("chatgames-internal-win")).setExecutor(new InternalCommand(this));

        // Register events
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this.gameHandler), this);

        this.getSLF4JLogger().info("ChatGames has been enabled!");
    }

    @Override
    public void onDisable() {
        if (this.gameHandler != null) {
            this.gameHandler.shutdown();
        }
        this.getSLF4JLogger().info("ChatGames has been disabled!");
    }

    public void reload() {
        this.getSLF4JLogger().info("Reloading ChatGames...");
        this.reloadConfig();
        this.languageHandler.load();
        this.gameHandler.reload();
        this.getSLF4JLogger().info("ChatGames reloaded successfully!");
    }

    public boolean logFull() {
        return Objects.equals(this.getConfig().getString("LOG_TYPE"), "FULL");
    }

    public GameHandler getGameHandler() {
        return this.gameHandler;
    }

}
