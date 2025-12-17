package me.RareHyperIon.ChatGames.games.types;

import me.RareHyperIon.ChatGames.ChatGames;
import me.RareHyperIon.ChatGames.games.Game;
import me.RareHyperIon.ChatGames.games.GameConfig;
import me.RareHyperIon.ChatGames.handlers.LanguageHandler;
import me.RareHyperIon.ChatGames.utility.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipleChoiceGame extends Game {

    private final Pattern pattern = Pattern.compile("^([A-H])\\.");

    private final Map.Entry<String, String> question;
    public final List<String> options = new ArrayList<>();
    public final int cooldown;

    public MultipleChoiceGame(final ChatGames plugin, final GameConfig config, final LanguageHandler language) {
        super(plugin, config, language);

        final GameConfig.MultipleChoiceQuestion randomQuestion =
                config.multipleChoiceQuestions.get(ThreadLocalRandom.current().nextInt(config.multipleChoiceQuestions.size()));

        final String questionText = randomQuestion.question + "\n" + String.join("\n", randomQuestion.answers);

        for(final String answer : randomQuestion.answers) {
            final Matcher matcher = pattern.matcher(answer);

            if(matcher.find()) {
                final String choice = matcher.group(1);
                this.options.add(choice.toLowerCase());
            } else {
                this.options.add(answer.trim().toLowerCase());
            }
        }

        this.question = new AbstractMap.SimpleEntry<>(questionText, randomQuestion.correctAnswer);
        this.cooldown = randomQuestion.cooldown;
    }

    @Override
    public void onStart() {
        if (this.plugin.logFull()) {
            this.plugin.getSLF4JLogger().info("Game \"{}\" has started.", this.config.name);
        }
        
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final String message = this.language.get("GameStart")
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
            final String message = this.language.get("GameWin")
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
            final String message = this.language.get("GameEnd")
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