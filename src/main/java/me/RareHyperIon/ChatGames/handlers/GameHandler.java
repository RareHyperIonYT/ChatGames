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
import java.util.Random;

public class GameHandler {

    private final ChatGames plugin;
    private final LanguageHandler language;

    private final List<GameConfig> games = new ArrayList<>();
    private final Random random = new Random();

    private int minimumPlayers, taskId;

    private ActiveGame game;
    private BukkitTask task;

    public GameHandler(final ChatGames plugin, final LanguageHandler language) {
        this.plugin = plugin;
        this.language = language;
    }

    public final void interval() {
        if(Bukkit.getOnlinePlayers().size() < this.minimumPlayers) return;
        if(this.game != null) return;

        final GameConfig config = this.games.get(this.random.nextInt(this.games.size()));
        this.game = new ActiveGame(this.plugin, config, this.language);

        this.task = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            this.game.end();
            this.game = null;
            this.task = null;
        }, config.timeout * 20L);
    }

    public final void win(final Player player) {
        this.task.cancel();
        this.task = null;

        this.game.win(player);
        this.game = null;
    }

    public final ActiveGame getGame() {
        return this.game;
    }

    public final void load() {
        final File folder = new File(this.plugin.getDataFolder(), "games");

        if(!folder.exists()) {
            this.saveDefault();
        }

        final File[] games = folder.listFiles(((dir, name) -> name.toLowerCase().endsWith(".yml")));

        if(games == null || games.length == 0) {
            ChatGames.LOGGER.warning("[ChatGames] There are no games to load.");
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

        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::interval, 0, interval * 20L);

        ChatGames.LOGGER.info("[ChatGames] Loaded games.");
    }

    private void saveDefault() {
        final File folder = new File(this.plugin.getDataFolder(), "games");
        if(!folder.mkdirs()) throw new IllegalStateException("Failed to create games folder.");

        for(final String game : List.of("trivia.yml", "math.yml", "unscramble.yml")) {
            final File out = new File(folder, game);

            try(final InputStream stream = this.plugin.getResource("games/" + game)) {
                if(stream == null) throw new IllegalStateException("Resource not found in jar.");
                Files.copy(stream, out.toPath());
            } catch (final IOException e) {
                throw new IllegalStateException("Failed to create default games file: " + game, e);
            }
        }

        ChatGames.LOGGER.info("[ChatGames] Created default game configurations.");
    }

    public final void reload() {
        this.games.clear();
        Bukkit.getScheduler().cancelTask(this.taskId);
        this.load();
    }

}
