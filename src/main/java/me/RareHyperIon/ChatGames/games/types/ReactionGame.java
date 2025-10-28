package me.RareHyperIon.ChatGames.games.types;

import me.RareHyperIon.ChatGames.ChatGames;
import me.RareHyperIon.ChatGames.games.Game;
import me.RareHyperIon.ChatGames.games.GameConfig;
import me.RareHyperIon.ChatGames.handlers.LanguageHandler;
import me.RareHyperIon.ChatGames.utility.Utility;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class ReactionGame extends Game {

    private final String word;

    public ReactionGame(final ChatGames plugin, final GameConfig config, final LanguageHandler language) {
        super(plugin, config, language);
        this.word = config.words.get(ThreadLocalRandom.current().nextInt(config.words.size()));
    }

    @Override
    public void onStart() {
        if (this.plugin.logFull()) {
            this.plugin.getSLF4JLogger().info("Game \"{}\" has started.", this.config.name);
        }

        final TextComponent messageComponent = new TextComponent(Utility.color(this.word));
        messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chatgames-internal-win"));

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final String message = this.language.get("ReactionStart")
                .replaceAll("\\{prefix}", this.language.get("Prefix"))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", this.config.name)
                .replaceAll("\\{timeout}", String.valueOf(this.config.timeout))
                .replaceAll("\\{descriptor}", this.config.descriptor)
                .replaceAll("\\n", "\n");

            final TextComponent finalMessage = new TextComponent(Utility.color(message));
            finalMessage.addExtra(messageComponent);

            player.spigot().sendMessage(finalMessage);
        }
    }

    @Override
    public void onWin(final Player player) {
        if (this.plugin.logFull()) {
            this.plugin.getSLF4JLogger().info("Player \"{}\" has won \"{}\"", player.getName(), this.config.name);
        }

        for (final Player online : Bukkit.getOnlinePlayers()) {
            final String message = this.language.get("ReactionWin")
                .replaceAll("\\{prefix}", this.language.get("Prefix"))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", this.config.name)
                .replaceAll("\\{descriptor}", this.config.descriptor)
                .replaceAll("\\n", "\n");

            online.sendMessage(Utility.colorComponent(message, online));
        }

        Bukkit.getScheduler().runTask(this.plugin, () -> {
            for (final String command : this.config.commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("\\{player}", player.getName()).replaceAll("%player%", player.getName()));
            }
        });
    }

    @Override
    public void onEnd() {
        if (this.plugin.logFull()) {
            this.plugin.getSLF4JLogger().info("Game \"{}\" has ended.", this.config.name);
        }

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final String message = this.language.get("ReactionEnd")
                .replaceAll("\\{prefix}", this.language.get("Prefix"))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", this.config.name)
                .replaceAll("\\{timeout}", String.valueOf(this.config.timeout))
                .replaceAll("\\{descriptor}", this.config.descriptor)
                .replaceAll("\\n", "\n");

            player.sendMessage(Utility.colorComponent(message, player));
        }
    }

}
