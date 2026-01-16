package dev.rarehyperion.chatgames.game;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.platform.PlatformPlayer;
import dev.rarehyperion.chatgames.platform.PlatformTask;
import dev.rarehyperion.chatgames.util.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GameManager {

    private static final String DEFAULT_COOLDOWN = "<red>You cannot answer this question as you've already tried recently.</red>";
    
    private final ChatGamesCore plugin;
    private final ConfigManager configManager;
    private final GameRegistry gameRegistry;

    private Game activeGame;
    private PlatformTask gameTimeoutTask;
    private PlatformTask schedulerTask;

    private final Map<UUID, Long> wrongAnswerCooldowns = new HashMap<>();

    public GameManager(final ChatGamesCore plugin, final ConfigManager configManager, final GameRegistry gameRegistry) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.gameRegistry = gameRegistry;
    }

    public void startScheduler() {
        if(!this.configManager.getSettings().automaticGames())
            return;

        final int intervalTicks = this.configManager.getSettings().gameInterval() * 20;
        this.schedulerTask = this.plugin.platform().runTaskTimer(this::tryStartRandomGame, intervalTicks, intervalTicks);
    }

    public void startGame(final GameConfig config) {
        if(this.activeGame != null) {
            this.plugin.platform().getLogger().warn("Cannot start game - one is already active!");
            return;
        }

        try {
            this.activeGame = this.gameRegistry.createGame(config);
            this.activeGame.onStart();

            // Scheduling a timeout
            final long timeoutTicks = config.getTimeoutSeconds() * 20L;
            this.gameTimeoutTask = this.plugin.platform().runTaskLater(this::endGameTimeout, timeoutTicks);

            if(this.configManager.getSettings().debug()) {
                this.plugin.platform().getLogger().info("Started game: " + config.getName());
            }
        } catch (final Exception exception) {
            this.plugin.platform().getLogger().error("Failed to start game: " + config.getName());
            exception.printStackTrace(System.err);
            this.activeGame = null;
        }
    }

    public void stopGame() {
        if(this.activeGame != null) {
            this.activeGame.onEnd();
            this.cancelTimeoutTask();
            this.activeGame = null;
            this.wrongAnswerCooldowns.clear();
        }
    }

    public boolean processAnswer(final PlatformPlayer player, final String answer) {
        if(this.activeGame == null) {
            return false;
        }

        final UUID uuid = player.id();

        if(this.activeGame.checkAnswer(answer)) {
            if(this.isOnCooldown(uuid)) {
                player.sendMessage(MessageUtil.parse(this.configManager.getMessage("cooldown", DEFAULT_COOLDOWN)));
                return true;
            }

            this.endGameWin(player);
            return true;
        }

        if(!this.activeGame.getAnswerOptions().isEmpty()) {
            final String lowerAnswer = answer.toLowerCase();

            if(this.activeGame.getAnswerOptions().contains(lowerAnswer)) {
                if(this.wrongAnswerCooldowns.containsKey(uuid)) {
                    player.sendMessage(MessageUtil.parse(this.configManager.getMessage("cooldown", DEFAULT_COOLDOWN)));
                }

                this.wrongAnswerCooldowns.put(uuid, System.currentTimeMillis());
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

        final int onlinePlayers = this.plugin.platform().getOnlinePlayers().size();
        if(onlinePlayers < this.configManager.getSettings().minimumPlayers()) return;

        this.gameRegistry.getRandomConfig().ifPresent(this::startGame);
    }

    private void endGameWin(final PlatformPlayer winner) {
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
