package me.RareHyperIon.ChatGames.games.types;

import me.RareHyperIon.ChatGames.ChatGames;
import me.RareHyperIon.ChatGames.games.Game;
import me.RareHyperIon.ChatGames.games.GameConfig;
import me.RareHyperIon.ChatGames.handlers.LanguageHandler;
import me.RareHyperIon.ChatGames.utility.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MultipleChoiceGame extends Game {

    private final Map.Entry<String, String> question;

    public MultipleChoiceGame(final ChatGames plugin, final GameConfig config, final LanguageHandler language) {
        super(plugin, config, language);

        // Get a random question from the list
        final GameConfig.MultipleChoiceQuestion randomQuestion = config.multipleChoiceQuestions.get(ThreadLocalRandom.current().nextInt(config.multipleChoiceQuestions.size()));

        final String questionText = randomQuestion.question + "\n" + String.join("\n", randomQuestion.answers);
        this.question = new AbstractMap.SimpleEntry<>(questionText, randomQuestion.correctAnswer);
    }

    @Override
    public void onStart() {
        if (this.plugin.logFull()) {
            this.plugin.getSLF4JLogger().info("Game \"{}\" has started.", this.config.name);
        }

        final String descriptor = this.config.descriptor == null ? "" : this.config.descriptor;
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final String message = this.language.get("GameStart")
                .replaceAll("\\{prefix}", this.language.get("Prefix"))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", this.config.displayName)
                .replaceAll("\\{timeout}", String.valueOf(this.config.timeout))
                .replaceAll("\\{descriptor}", descriptor)
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

        final String descriptor = this.config.descriptor == null ? "" : this.config.descriptor;
        for (final Player online : Bukkit.getOnlinePlayers()) {
            final String message = this.language.get("GameWin")
                .replaceAll("\\{prefix}", this.language.get("Prefix"))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", this.config.displayName)
                .replaceAll("\\{descriptor}", descriptor)
                .replaceAll("\\{question}", this.question.getKey())
                .replaceAll("\\{answer}", this.question.getValue())
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

        final String descriptor = this.config.descriptor == null ? "" : this.config.descriptor;
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final String message = this.language.get("GameEnd")
                .replaceAll("\\{prefix}", this.language.get("Prefix"))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", this.config.displayName)
                .replaceAll("\\{timeout}", String.valueOf(this.config.timeout))
                .replaceAll("\\{descriptor}", descriptor)
                .replaceAll("\\{question}", this.question.getKey())
                .replaceAll("\\{answer}", this.question.getValue())
                .replaceAll("\\n", "\n");

            player.sendMessage(Utility.colorComponent(message, player));
        }
    }

    @Override
    public Map.Entry<String, String> getQuestion() {
        return this.question;
    }
}