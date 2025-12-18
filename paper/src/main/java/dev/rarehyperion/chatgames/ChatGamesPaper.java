package dev.rarehyperion.chatgames;

import dev.rarehyperion.chatgames.command.ChatGamesCommand;
import dev.rarehyperion.chatgames.listener.PaperChatListener;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@SuppressWarnings("UnstableApiUsage")
public final class ChatGamesPaper extends AbstractChatGames {

    @Override
    public void sendMessage(final CommandSender sender, final Component component) {
        sender.sendMessage(component);
    }

    @Override
    public void broadcast(final Component component) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(component));
    }
    @Override
    public void registerListeners() {
        this.getServer().getPluginManager().registerEvents(new PaperChatListener(this.gameManager), this);
    }

    @Override
    public void registerCommands() {
        // Papers API genuinely drives me insane... almost as much as Fabric API which says a lot.

        final ChatGamesCommand command = new ChatGamesCommand(this);
        final var mainNode = command.build();

        // Aliases don't work as expected, so Paper won't have command aliases for now unfortunately.
//      final var aliasCg = Commands.literal("cg").redirect(mainNode).build();
//      final var aliasChatgame = Commands.literal("chatgame").redirect(mainNode).build();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(mainNode);
//          commands.register(aliasCg);
//          commands.register(aliasChatgame);
        });
    }

    @Override
    public String getPlatformName() {
        return "PAPER";
    }
}