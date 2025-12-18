package dev.rarehyperion.chatgames;

import dev.rarehyperion.chatgames.command.SpigotChatGamesCommand;
import dev.rarehyperion.chatgames.listener.SpigotChatListener;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public class ChatGamesSpigot extends AbstractChatGames {

    @Override
    public void sendMessage(final CommandSender sender, final Component component) {
        final String legacyMessage = MessageUtil.serialize(component);
        sender.sendMessage(legacyMessage);
    }

    @Override
    public void broadcast(final Component component) {
        final String message = MessageUtil.serialize(component);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
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
        final SpigotChatGamesCommand command = new SpigotChatGamesCommand(this);
        Objects.requireNonNull(this.getCommand("chatgames")).setExecutor(command);
        Objects.requireNonNull(this.getCommand("chatgames")).setTabCompleter(command);
    }

    @Override
    public String getPlatformName() {
        return "SPIGOT";
    }


}
