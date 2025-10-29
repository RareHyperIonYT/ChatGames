package me.RareHyperIon.ChatGames.handlers;

import me.RareHyperIon.ChatGames.ChatGames;
import me.RareHyperIon.ChatGames.games.ActiveGame;
import me.RareHyperIon.ChatGames.games.GameConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameHandler {

    private final ChatGames plugin;
    private final LanguageHandler language;

    private final List<GameConfig> games = new ArrayList<>();

    private int minimumPlayers;
    private BukkitTask intervalTask;
    private boolean automaticGames;

    private ActiveGame game;
    private BukkitTask gameTask;

    public GameHandler(final ChatGames plugin, final LanguageHandler language) {
        this.plugin = plugin;
        this.language = language;
    }

    public final void interval() {
        if(!this.automaticGames) return;
        if(Bukkit.getOnlinePlayers().size() < this.minimumPlayers) return;
        if(this.game != null) return;

        final GameConfig config = this.games.get(ThreadLocalRandom.current().nextInt(this.games.size()));
        this.startGame(config);
    }

    public final void win(final Player player) {
        if (this.gameTask != null) {
            this.gameTask.cancel();
            this.gameTask = null;
        }

        if (this.game != null) {
            this.game.win(player);
            this.game = null;
        }
    }

    /**
     * Attempts to win the game by validating the player's answer.
     * If the answer is incorrect, the game continues.
     */
    public final void attemptWin(final Player player, final String answer) {
        if (this.game == null) {
            return;
        }

        // Get the expected answer from the game
        final java.util.Map.Entry<String, String> questionAnswer = this.game.getGame().getQuestion();
        if (questionAnswer == null || questionAnswer.getValue() == null) {
            // No validation needed, accept any answer
            win(player);
            return;
        }

        final String correctAnswer = questionAnswer.getValue();

        // Validate the answer (case-insensitive, strip color codes)
        if (answer.equalsIgnoreCase(correctAnswer)) {
            win(player);
        }
        // If incorrect, do nothing (game continues)
    }

    public final ActiveGame getGame() {
        return this.game;
    }

    public final List<GameConfig> getGames() {
        return this.games;
    }

    public final void load() {
        final File folder = new File(this.plugin.getDataFolder(), "games");

        if(!folder.exists()) {
            this.saveDefault();
        }

        final File[] games = folder.listFiles(((dir, name) -> name.toLowerCase().endsWith(".yml")));

        if(games == null || games.length == 0) {
            this.plugin.getSLF4JLogger().warn("There are no games to load.");
            return;
        }

        for(final File file : games) {
            if(!file.isFile() || !file.getName().toLowerCase().endsWith(".yml")) continue;

            final GameConfig config = new GameConfig(YamlConfiguration.loadConfiguration(file));
            this.games.add(config);
        }

        final FileConfiguration pluginConfig = this.plugin.getConfig();
        final int interval = pluginConfig.getInt("GameInterval");

        this.minimumPlayers = pluginConfig.getInt("MinimumPlayers");
        this.automaticGames = pluginConfig.getBoolean("AutomaticGames");

        this.intervalTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this::interval, 0L, interval * 20L);

        this.plugin.getSLF4JLogger().info("Loaded {} game(s).", this.games.size());
    }

    private void saveDefault() {
        final File folder = new File(this.plugin.getDataFolder(), "games");
        if(!folder.mkdirs()) throw new IllegalStateException("Failed to create games folder.");

        for(final String game : List.of("trivia.yml", "math.yml", "unscramble.yml", "reaction.yml", "multiple_choice.yml")) {
            final File out = new File(folder, game);

            try(final InputStream stream = this.plugin.getResource("games/" + game)) {
                if(stream == null) throw new IllegalStateException("Resource not found in jar.");
                Files.copy(stream, out.toPath());
            } catch (final IOException e) {
                throw new IllegalStateException("Failed to create default games file: " + game, e);
            }
        }

        this.plugin.getSLF4JLogger().info("Created default game configurations.");
    }

    public final void reload() {
        this.shutdown();
        this.games.clear();
        this.load();
    }

    public final void shutdown() {
        if (this.intervalTask != null) {
            this.intervalTask.cancel();
            this.intervalTask = null;
        }
        if (this.gameTask != null) {
            this.gameTask.cancel();
            this.gameTask = null;
        }
        if (this.game != null) {
            this.game = null;
        }
    }

    public void startGame(final GameConfig config) {
        if(this.game != null) return;
        this.game = new ActiveGame(this.plugin, config, this.language);

        this.gameTask = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (this.game != null) {
                this.game.end();
                this.game = null;
            }
            this.gameTask = null;
        }, config.timeout * 20L);
    }

    public void stopGame() {
        if (this.gameTask != null) {
            this.gameTask.cancel();
            this.gameTask = null;
        }

        if (this.game != null) {
            this.game.end();
            this.game = null;
        }
    }

    public void setAutomaticGames(final boolean automaticGames) {
        this.automaticGames = automaticGames;
    }

}
