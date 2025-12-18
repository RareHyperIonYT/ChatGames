package dev.rarehyperion.chatgames;

import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.rarehyperion.chatgames.command.ChatGamesCommand;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.game.GameManager;
import dev.rarehyperion.chatgames.game.GameRegistry;
import dev.rarehyperion.chatgames.listener.PaperChatListener;
import dev.rarehyperion.chatgames.platform.ChatGamesPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatGamesPaper extends JavaPlugin implements ChatGamesPlugin {

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
        base.onPluginEnable();
    }

    @Override
    public void onDisable() {
        base.onPluginDisable();
    }

    @Override
    public void broadcast(final Component component) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(component));
    }

    @Override
    public void sendMessage(final Player player, final Component component) {
        player.sendMessage(component);
    }

    @Override
    public void sendMessage(final CommandSender sender, final Component component) {
        sender.sendMessage(component);
    }

    @Override
    public void registerListeners() {
        getServer().getPluginManager().registerEvents(
                new PaperChatListener(this.gameManager),
                this
        );
    }

    @Override
    public void registerCommands() {
        // Papers API genuinely drives me insane... almost as much as Fabric API which says a lot.
        final ChatGamesCommand command = new ChatGamesCommand(this);

        final LiteralCommandNode<CommandSourceStack> mainNode = command.build();
        final LiteralCommandNode<CommandSourceStack> aliasCg = Commands.literal("cg").redirect(mainNode).build();
        final LiteralCommandNode<CommandSourceStack> aliasChatgame = Commands.literal("chatgame").redirect(mainNode).build();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(mainNode);
            commands.register(aliasCg);
            commands.register(aliasChatgame);
        });
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
        return "PAPER";
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