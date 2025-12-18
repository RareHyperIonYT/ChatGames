package dev.rarehyperion.chatgames.platform;

import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.game.GameManager;
import dev.rarehyperion.chatgames.game.GameRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public interface ChatGamesPlugin extends Plugin {

    void broadcast(final Component component);
    void sendMessage(final Player player, final Component component);
    void sendMessage(final CommandSender sender, final Component component);

    void registerListeners();
    void reload();

    @NotNull Logger getLogger();

    ConfigManager getConfigManager();
    GameManager getGameManager();
    GameRegistry getGameRegistry();

    void setConfigManager(ConfigManager configManager);
    void setGameManager(GameManager gameManager);
    void setGameRegistry(GameRegistry gameRegistry);
    void registerCommands();

    String getPlatformName();

}


