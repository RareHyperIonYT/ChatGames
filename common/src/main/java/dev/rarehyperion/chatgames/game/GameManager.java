package dev.rarehyperion.chatgames.game;

import dev.rarehyperion.chatgames.AbstractChatGames;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GameManager {

    private final AbstractChatGames plugin;
    private final ConfigManager configManager;
    private final GameRegistry gameRegistry;

    private Game activeGame;
    private BukkitTask gameTimeoutTask;
    private BukkitTask schedulerTask;

    private final Map<UUID, Long> wrongAnswerCooldowns = new HashMap<>();

    public GameManager(final AbstractChatGames plugin, final ConfigManager configManager, final GameRegistry gameRegistry) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.gameRegistry = gameRegistry;
    }

    public void startScheduler() {
        if(!this.configManager.getSettings().automaticGames())
            return;

        final int intervalTicks = this.configManager.getSettings().gameInterval() * 20;
        this.schedulerTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tryStartRandomGame, intervalTicks, intervalTicks);
    }

    public void startGame(final GameConfig config) {
        if(this.activeGame != null) {
            this.plugin.getLogger().warning("Cannot start game - one is already active!");
            return;
        }

        try {
            this.activeGame = this.gameRegistry.createGame(config);
            this.activeGame.start();

            // Scheduling a timeout
            final long timeoutTicks = config.getTimeoutSeconds() * 20L;
            this.gameTimeoutTask = Bukkit.getScheduler().runTaskLater(plugin, this::endGameTimeout, timeoutTicks);

            if(this.configManager.getSettings().debug()) {
                this.plugin.getLogger().info("Started game: " + config.getName());
            }
        } catch (final Exception exception) {
            this.plugin.getLogger().severe("Failed to start game: " + config.getName());
            exception.printStackTrace(System.err);
            this.activeGame = null;
        }
    }

    public void stopGame() {
        if(this.activeGame != null) {
            this.cancelTimeoutTask();
            this.activeGame = null;
            this.wrongAnswerCooldowns.clear();
        }
    }

    public boolean processAnswer(final Player player, final String answer) {
        if(this.activeGame == null) {
            return false;
        }

        final UUID playerId = player.getUniqueId();

        if(this.activeGame.checkAnswer(answer)) {
            if(this.isOnCooldown(playerId)) {
                this.plugin.sendMessage(player, MessageUtil.parse(this.configManager.getMessage("cooldown", "<red>You cannot answer this question as you've already tried recently.</red>")));
                return true;
            }

            this.endGameWin(player);
            return true;
        }

        if(!this.activeGame.getAnswerOptions().isEmpty()) {
            final String lowerAnswer = answer.toLowerCase();

            if(this.activeGame.getAnswerOptions().contains(lowerAnswer)) {
                this.wrongAnswerCooldowns.put(playerId, System.currentTimeMillis());
                return true;
            }


        }

        return false;
    }

    public void reload() {
        this.stopGame();
        this.shutdown();

        this.gameRegistry.loadGames();

        this.startScheduler();
    }

    public void shutdown() {
        this.stopGame();

        if(this.schedulerTask != null) {
            this.schedulerTask.cancel();
            this.schedulerTask = null;
        }
    }

    private void tryStartRandomGame() {
        if(this.activeGame != null) return;

        final int onlinePlayers = Bukkit.getOnlinePlayers().size();
        if(onlinePlayers < this.configManager.getSettings().minimumPlayers()) return;

        this.gameRegistry.getRandomConfig().ifPresent(this::startGame);
    }

    private void endGameWin(final Player winner) {
        if(this.activeGame == null) {
            return;
        }

        this.cancelTimeoutTask();
        this.activeGame.onWin(winner);
        this.activeGame = null;
        this.wrongAnswerCooldowns.clear();
    }

    private void endGameTimeout() {
        if(this.activeGame == null) {
            return;
        }

        this.activeGame.onTimeout();
        this.activeGame = null;
        this.wrongAnswerCooldowns.clear();
    }

    private void cancelTimeoutTask() {
        if(this.gameTimeoutTask != null) {
            this.gameTimeoutTask.cancel();
            this.gameTimeoutTask = null;
        }
    }

    private boolean isOnCooldown(final UUID playerId) {
        if(!this.wrongAnswerCooldowns.containsKey(playerId)) {
            return false;
        }

        final long lastAttempt = this.wrongAnswerCooldowns.get(playerId);
        final long currentTime = System.currentTimeMillis();
        final long cooldownMillis = this.configManager.getSettings().answerCooldownTicks() * 50L;

        return (currentTime - lastAttempt) < cooldownMillis;
    }

    public Game getActiveGame() {
        return this.activeGame;
    }

}
