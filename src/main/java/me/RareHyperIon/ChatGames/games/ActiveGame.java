package me.RareHyperIon.ChatGames.games;

import me.RareHyperIon.ChatGames.ChatGames;
import me.RareHyperIon.ChatGames.handlers.LanguageHandler;
import me.RareHyperIon.ChatGames.utility.Utility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Random;

public class ActiveGame {

    private final LanguageHandler language;
    private final GameConfig config;
    private final ChatGames plugin;

    public final Map.Entry<String, String> question;

    public ActiveGame(final ChatGames plugin, final GameConfig config, final LanguageHandler language) {
        this.plugin = plugin;
        this.config = config;
        this.language = language;

        final Random random = new Random();

        this.question = config.choices.get(random.nextInt(config.choices.size()));

        this.start();
    }

    public void start() {
        if(this.plugin.logFull()) ChatGames.LOGGER.info(Utility.format("Game \"{}\" has started.",this.config.name));

        for(final Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(Utility.color(
                this.language.get("GameStart")
                    .replaceAll("\\{prefix}", this.language.get("Prefix"))
                    .replaceAll("\\{player}", player.getName())
                    .replaceAll("\\{name}", this.config.name)
                    .replaceAll("\\{timeout}", String.valueOf(this.config.timeout))
                    .replaceAll("\\{descriptor}", this.config.descriptor)
                    .replaceAll("\\{question}", this.question.getKey())
                    .replaceAll("\\n", "\n")
            ));
        }
    }

    public void end() {
        if(this.plugin.logFull()) ChatGames.LOGGER.info(Utility.format("Game \"{}\" has ended.",this.config.name));

        for(final Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(Utility.color(
                    this.language.get("GameEnd")
                            .replaceAll("\\{prefix}", this.language.get("Prefix"))
                            .replaceAll("\\{player}", player.getName())
                            .replaceAll("\\{name}", this.config.name)
                            .replaceAll("\\{timeout}", String.valueOf(this.config.timeout))
                            .replaceAll("\\{descriptor}", this.config.descriptor)
                            .replaceAll("\\{question}", this.question.getKey())
                            .replaceAll("\\n", "\n")
            ));
        }
    }

    public void win(final Player player) {
        if(this.plugin.logFull()) ChatGames.LOGGER.info(Utility.format("Player \"{}\" has won \"{}\"", player.getName(), this.config.name));

        for(final Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(Utility.color(
                    this.language.get("GameWin")
                            .replaceAll("\\{prefix}", this.language.get("Prefix"))
                            .replaceAll("\\{player}", player.getName())
                            .replaceAll("\\{name}", this.config.name)
                            .replaceAll("\\{descriptor}", this.config.descriptor)
                            .replaceAll("\\{question}", this.question.getKey())
                            .replaceAll("\\n", "\n")
            ));
        }

        Bukkit.getScheduler().runTask(this.plugin, () -> {
            for(final String command : this.config.commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("\\{player}", player.getName()));
            }
        });
    }


}
