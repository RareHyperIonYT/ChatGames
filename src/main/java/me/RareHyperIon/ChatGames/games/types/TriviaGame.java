package me.RareHyperIon.ChatGames.games.types;

import me.RareHyperIon.ChatGames.ChatGames;
import me.RareHyperIon.ChatGames.games.Game;
import me.RareHyperIon.ChatGames.games.GameConfig;
import me.RareHyperIon.ChatGames.handlers.LanguageHandler;
import me.RareHyperIon.ChatGames.utility.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class TriviaGame extends Game {

    public final Map.Entry<String, String> question;

    public TriviaGame(final ChatGames plugin, final GameConfig config, final LanguageHandler language) {
        super(plugin, config, language);
        this.question = config.choices.get(ThreadLocalRandom.current().nextInt(config.choices.size()));
    }

    @Override
    public void onStart() {
        if (this.plugin.logFull()) {
            this.plugin.getSLF4JLogger().info("Game \"{}\" has started.", this.config.name);
        }

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final String message = this.language.get("GameStart")
                .replaceAll("\\{prefix}", this.language.get("Prefix"))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", this.config.name)
                .replaceAll("\\{timeout}", String.valueOf(this.config.timeout))
                .replaceAll("\\{descriptor}", this.config.descriptor)
                .replaceAll("\\{question}", this.question.getKey())
                .replaceAll("\\n", "\n");

            player.sendMessage(Utility.colorComponent(message, player));
        }
    }

    @Override
    public void onWin(final Player player) {
        if (this.plugin.logFull()) {
            this.plugin.getSLF4JLogger().info("Player \"{}\" has won \"{}\"", player.getName(), this.config.name);
        }

        for (final Player online : Bukkit.getOnlinePlayers()) {
            final String message = this.language.get("GameWin")
                .replaceAll("\\{prefix}", this.language.get("Prefix"))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", this.config.name)
                .replaceAll("\\{descriptor}", this.config.descriptor)
                .replaceAll("\\{question}", this.question.getKey())
                .replaceAll("\\n", "\n");

            online.sendMessage(Utility.colorComponent(message, online));
        }

        Bukkit.getScheduler().runTask(this.plugin, () -> {
            for (final String command : this.config.commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("\\{player}", player.getName()));
            }
        });
    }

    @Override
    public void onEnd() {
        if (this.plugin.logFull()) {
            this.plugin.getSLF4JLogger().info("Game \"{}\" has ended.", this.config.name);
        }

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final String message = this.language.get("GameEnd")
                .replaceAll("\\{prefix}", this.language.get("Prefix"))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", this.config.name)
                .replaceAll("\\{timeout}", String.valueOf(this.config.timeout))
                .replaceAll("\\{descriptor}", this.config.descriptor)
                .replaceAll("\\{question}", this.question.getKey())
                .replaceAll("\\n", "\n");

            player.sendMessage(Utility.colorComponent(message, player));
        }
    }

    @Override
    public Map.Entry<String, String> getQuestion() {
        return this.question;
    }

}
