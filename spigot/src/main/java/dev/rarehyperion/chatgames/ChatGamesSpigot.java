package dev.rarehyperion.chatgames;

import dev.rarehyperion.chatgames.command.ChatGamesCommand;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.game.GameManager;
import dev.rarehyperion.chatgames.game.GameRegistry;
import dev.rarehyperion.chatgames.listener.SpigotChatListener;
import dev.rarehyperion.chatgames.platform.ChatGamesPlugin;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class ChatGamesSpigot extends JavaPlugin implements ChatGamesPlugin {

    private ChatGamesBase base;
    private ConfigManager configManager;
    private GameRegistry gameRegistry;
    private GameManager gameManager;

    @Override
    public void onLoad() {
        this.base = new ChatGamesBase(this) {};
        this.base.onPluginLoad();
    }

    @Override
    public void onEnable() {
        this.base.onPluginEnable();
    }

    @Override
    public void onDisable() {
        this.base.onPluginDisable();
    }

    @Override
    public void broadcast(final Component component) {
        // Spigot doesn't have native Adventure support, so we serialize to legacy
        final String legacyMessage = MessageUtil.serialize(component);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(legacyMessage));
    }

    @Override
    public void sendMessage(final Player player, final Component component) {
        final String legacyMessage = MessageUtil.serialize(component);
        player.sendMessage(legacyMessage);
    }

    @Override
    public void sendMessage(final CommandSender sender, final Component component) {
        final String legacyMessage = MessageUtil.serialize(component);
        sender.sendMessage(legacyMessage);
    }

    @Override
    public void registerListeners() {
        this.getServer().getPluginManager().registerEvents(
                new SpigotChatListener(this.gameManager),
                this
        );
    }

    @Override
    public void registerCommands() {
        final ChatGamesCommand command = new ChatGamesCommand(this);
        Objects.requireNonNull(this.getCommand("chatgames")).setExecutor(command);
        Objects.requireNonNull(this.getCommand("chatgames")).setTabCompleter(command);
    }

    @Override
    public void reload() {
        this.getLogger().info("Reloading ChatGames...");
        this.reloadConfig();
        this.configManager.load();
        this.gameManager.reload();
        this.getLogger().info("ChatGames reloaded successfully");
    }

    @Override
    public String getPlatformName() {
        return "SPIGOT";
    }

    @Override
    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    @Override
    public GameManager getGameManager() {
        return this.gameManager;
    }

    @Override
    public GameRegistry getGameRegistry() {
        return this.gameRegistry;
    }


    @Override
    public void setConfigManager(final ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void setGameManager(final GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void setGameRegistry(final GameRegistry gameRegistry) {
        this.gameRegistry = gameRegistry;
    }

}
