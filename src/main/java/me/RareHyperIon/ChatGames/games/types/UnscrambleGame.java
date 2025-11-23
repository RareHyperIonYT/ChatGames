package me.RareHyperIon.ChatGames.games.types;

import me.RareHyperIon.ChatGames.ChatGames;
import me.RareHyperIon.ChatGames.games.Game;
import me.RareHyperIon.ChatGames.games.GameConfig;
import me.RareHyperIon.ChatGames.handlers.LanguageHandler;
import me.RareHyperIon.ChatGames.utility.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects; // Import Objects
import java.util.concurrent.ThreadLocalRandom;

public class UnscrambleGame extends Game {

    public final Map.Entry<String, String> question;

    public UnscrambleGame(final ChatGames plugin, final GameConfig config, final LanguageHandler language) {
        super(plugin, config, language);
        this.question = config.choices.get(ThreadLocalRandom.current().nextInt(config.choices.size()));
    }

    @Override
    public void onStart() {
        if (this.plugin.logFull()) {
            this.plugin.getSLF4JLogger().info("Game \"{}\" has started.", this.config.name);
        }

        for (final Player player : Bukkit.getOnlinePlayers()) {
            String message = this.language.get("GameStart");
            if (message == null) {
                message = ""; // Provide a default empty string if "GameStart" is null
            }
            message = message
                .replaceAll("\\{prefix}", Objects.toString(this.language.get("Prefix"), ""))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", Objects.toString(this.config.displayName, ""))
                .replaceAll("\\{timeout}", String.valueOf(this.config.timeout))
                .replaceAll("\\{descriptor}", Objects.toString(this.config.descriptor, ""))
                .replaceAll("\\{question}", Objects.toString(this.question.getKey(), ""))
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
            String message = this.language.get("GameWin");
            if (message == null) {
                message = ""; // Provide a default empty string if "GameWin" is null
            }
            message = message
                .replaceAll("\\{prefix}", Objects.toString(this.language.get("Prefix"), ""))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", Objects.toString(this.config.displayName, ""))
                .replaceAll("\\{descriptor}", Objects.toString(this.config.descriptor, ""))
                .replaceAll("\\{question}", Objects.toString(this.question.getKey(), ""))
                .replaceAll("\\{answer}", Objects.toString(this.question.getValue(), ""))
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
            String message = this.language.get("GameEnd");
            if (message == null) {
                message = ""; // Provide a default empty string if "GameEnd" is null
            }
            message = message
                .replaceAll("\\{prefix}", Objects.toString(this.language.get("Prefix"), ""))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", Objects.toString(this.config.displayName, ""))
                .replaceAll("\\{timeout}", String.valueOf(this.config.timeout))
                .replaceAll("\\{descriptor}", Objects.toString(this.config.descriptor, ""))
                .replaceAll("\\{question}", Objects.toString(this.question.getKey(), ""))
                .replaceAll("\\{answer}", Objects.toString(this.question.getValue(), ""))
                .replaceAll("\\n", "\n");

            player.sendMessage(Utility.colorComponent(message, player));
        }
    }

    @Override
    public Map.Entry<String, String> getQuestion() {
        return this.question;
    }

}