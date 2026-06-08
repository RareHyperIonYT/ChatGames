package dev.rarehyperion.chatgames.game;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.platform.PlatformPlayer;
import dev.rarehyperion.chatgames.platform.PlatformTask;
import dev.rarehyperion.chatgames.platform.RemotePlatformPlayer;
import dev.rarehyperion.chatgames.redis.DistributedGameService;
import dev.rarehyperion.chatgames.util.MessageUtil;
import dev.rarehyperion.chatgames.util.Templater;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GameManager {

    private static final String DEFAULT_COOLDOWN = "<red>You cannot answer this question as you've already tried recently.</red>";
    
    private final ChatGamesCore plugin;
    private final ConfigManager configManager;
    private final GameRegistry gameRegistry;

    private volatile Game activeGame;
    private volatile GameConfig activeConfig;
    private volatile GameSnapshot activeSnapshot;

    private PlatformTask gameTimeoutTask;
    private PlatformTask schedulerTask;
    private DistributedGameService distributedGameService;

    private final Map<UUID, Long> wrongAnswerCooldowns = new ConcurrentHashMap<>();
    private final Set<String> completedDistributedGames = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    public GameManager(final ChatGamesCore plugin, final ConfigManager configManager, final GameRegistry gameRegistry) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.gameRegistry = gameRegistry;
    }

    public void startScheduler() {
        this.startDistributedServiceIfNeeded();

        if(this.schedulerTask != null) {
            this.schedulerTask.cancel();
            this.schedulerTask = null;
        }

        if(!this.configManager.getSettings().automaticGames())
            return;

        if(this.isDistributedModeConfigured() && !this.configManager.getSettings().distributedAutomaticLeader()) {
            if(this.configManager.getSettings().debug()) {
                this.plugin.platform().getLogger().info("Automatic games are disabled on this server because multi-server.leader is false.");
            }
            return;
        }

        final int intervalTicks = this.configManager.getSettings().gameInterval() * 20;
        this.schedulerTask = this.plugin.platform().runTaskTimer(this::tryStartRandomGame, intervalTicks, intervalTicks);
    }

    public void startGame(final GameConfig config) {
        if(this.isDistributedModeConfigured()) {
            if(this.distributedGameService == null || !this.distributedGameService.isRunning()) {
                this.plugin.platform().getLogger().warn("Cannot start distributed game because Redis multi-server mode is not running.");
                return;
            }

            this.distributedGameService.requestManualStart(config);
            return;
        }

        if(this.activeGame != null) {
            this.plugin.platform().getLogger().warn("Cannot start game - one is already active!");
            return;
        }

        try {
            this.activeGame = this.gameRegistry.createGame(config);
            this.activeConfig = config;
            this.activeSnapshot = null;
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
            this.activeConfig = null;
        }
    }

    public void stopGame() {
        if(this.isDistributedModeConfigured()) {
            if(this.distributedGameService != null && this.distributedGameService.isRunning()) {
                this.distributedGameService.requestStop();
            } else {
                this.plugin.platform().getLogger().warn("Cannot stop distributed game because Redis multi-server mode is not running.");
            }

            return;
        }

        if(this.activeGame != null) {
            this.activeGame.onEnd();
            this.clearLocalGame();
        }
    }

    public boolean processAnswer(final PlatformPlayer player, final String answer) {
        if(this.isDistributedModeConfigured()) {
            return this.processDistributedAnswer(player, answer);
        }

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
                if(this.isOnCooldown(uuid)) {
                    player.sendMessage(MessageUtil.parse(this.configManager.getMessage("cooldown", DEFAULT_COOLDOWN)));
                }

                this.wrongAnswerCooldowns.put(uuid, System.currentTimeMillis());
                return true;
            }
        }

        return false;
    }

    public void reload() {
        this.clearLocalGame();
        this.stopScheduler();
        this.stopDistributedService();

        this.gameRegistry.loadGames();

        this.startScheduler();
    }

    public void shutdown() {
        this.clearLocalGame();
        this.stopScheduler();
        this.stopDistributedService();
    }

    public void stopScheduler() {
        if(this.schedulerTask != null) {
            this.schedulerTask.cancel();
            this.schedulerTask = null;
        }
    }

    public boolean isReactionGameActive() {
        final Game game = this.activeGame;
        return game != null && game.getType() == GameType.REACTION;
    }

    public boolean isDistributedGameActive(final String gameId) {
        return this.activeGame != null && this.activeSnapshot != null && this.activeSnapshot.gameId().equals(gameId);
    }

    public boolean isDistributedModeConfigured() {
        return this.configManager.getSettings() != null && this.configManager.getSettings().distributedGames();
    }

    public void activateDistributedGame(final GameSnapshot snapshot, final boolean announce) {
        if(snapshot == null) {
            return;
        }

        this.plugin.platform().runTask(() -> {
            if(this.completedDistributedGames.contains(snapshot.gameId())) {
                return;
            }

            if(this.activeSnapshot != null && this.activeSnapshot.gameId().equals(snapshot.gameId()) && this.activeGame != null) {
                return;
            }

            final Optional<GameConfig> optionalConfig = this.gameRegistry.getConfigByName(snapshot.configName());
            if(!optionalConfig.isPresent()) {
                this.plugin.platform().getLogger().warn("Cannot activate distributed game '" + snapshot.configName() + "' because the config is missing on this server.");
                return;
            }

            this.clearLocalGame();
            this.completedDistributedGames.clear();
            this.activeConfig = optionalConfig.get();
            this.activeSnapshot = snapshot;
            this.activeGame = new SharedGame(this.plugin, this.activeConfig, snapshot);

            if(announce) {
                this.activeGame.onStart();
            }
        });
    }

    public void handleDistributedWin(final GameSnapshot snapshot, final UUID winnerId, final String winnerName, final boolean rewardClaimed) {
        if(snapshot == null) {
            return;
        }

        this.plugin.platform().runTask(() -> {
            final boolean firstCompletion = this.completedDistributedGames.add(snapshot.gameId());
            if(!firstCompletion && !rewardClaimed) {
                return;
            }

            final Optional<GameConfig> optionalConfig = this.gameRegistry.getConfigByName(snapshot.configName());
            if(!optionalConfig.isPresent()) {
                this.plugin.platform().getLogger().warn("Cannot complete distributed game '" + snapshot.configName() + "' because the config is missing on this server.");
                return;
            }

            final GameConfig config = optionalConfig.get();
            final PlatformPlayer winner = new RemotePlatformPlayer(winnerId, winnerName);
            final boolean winnerOnlineHere = this.plugin.platform().getOnlinePlayers().contains(winnerId);

            if(firstCompletion) {
                this.plugin.broadcast(config.getWinMessage(winnerName, snapshot.displayAnswer().orElse("Unknown")));
                this.clearMatchingDistributedGame(snapshot.gameId());
            }

            if(rewardClaimed) {
                this.runRewards(config, winner);
            }

            if(firstCompletion && winnerOnlineHere) {
                this.plugin.platform().dispatchWin(winner, snapshot.type(), this.plainQuestion(config, snapshot), snapshot.displayAnswer().orElse(null), config.getRewardCommands());
            }
        });
    }

    public void handleDistributedTimeout(final GameSnapshot snapshot) {
        if(snapshot == null) {
            return;
        }

        this.plugin.platform().runTask(() -> {
            if(!this.completedDistributedGames.add(snapshot.gameId())) {
                return;
            }

            final Optional<GameConfig> optionalConfig = this.gameRegistry.getConfigByName(snapshot.configName());
            if(!optionalConfig.isPresent()) {
                this.plugin.platform().getLogger().warn("Cannot timeout distributed game '" + snapshot.configName() + "' because the config is missing on this server.");
                return;
            }

            final GameConfig config = optionalConfig.get();
            this.plugin.broadcast(config.getTimeoutMessage(snapshot.displayAnswer().orElse("Unknown")));
            this.plugin.platform().dispatchEnd(snapshot.type(), this.plainQuestion(config, snapshot), snapshot.displayAnswer().orElse(null), config.getRewardCommands(), EndReason.TIMEOUT);
            this.clearMatchingDistributedGame(snapshot.gameId());
        });
    }

    public void handleDistributedStop(final GameSnapshot snapshot) {
        if(snapshot == null) {
            return;
        }

        this.plugin.platform().runTask(() -> {
            if(!this.completedDistributedGames.add(snapshot.gameId())) {
                return;
            }

            final Optional<GameConfig> optionalConfig = this.gameRegistry.getConfigByName(snapshot.configName());
            if(optionalConfig.isPresent()) {
                final GameConfig config = optionalConfig.get();
                this.plugin.platform().dispatchEnd(snapshot.type(), this.plainQuestion(config, snapshot), snapshot.displayAnswer().orElse(null), config.getRewardCommands(), EndReason.COMMAND);
            }

            this.clearMatchingDistributedGame(snapshot.gameId());
        });
    }

    private void tryStartRandomGame() {
        if(this.isDistributedModeConfigured()) {
            if(this.distributedGameService == null || !this.distributedGameService.isRunning()) {
                return;
            }

            if(!this.configManager.getSettings().distributedAutomaticLeader()) {
                return;
            }

            this.gameRegistry.getRandomConfig().ifPresent(config ->
                    this.distributedGameService.requestAutomaticStart(config, this.configManager.getSettings().minimumPlayers())
            );
            return;
        }

        if(this.activeGame != null) return;

        final int onlinePlayers = this.plugin.platform().getOnlinePlayers().size();
        if(onlinePlayers < this.configManager.getSettings().minimumPlayers()) return;

        this.gameRegistry.getRandomConfig().ifPresent(this::startGame);
    }

    private boolean processDistributedAnswer(final PlatformPlayer player, final String answer) {
        final Game game = this.activeGame;
        final GameSnapshot snapshot = this.activeSnapshot;

        if(game == null || snapshot == null) {
            return false;
        }

        final UUID uuid = player.id();

        if(game.checkAnswer(answer)) {
            if(this.isOnCooldown(uuid)) {
                player.sendMessage(MessageUtil.parse(this.configManager.getMessage("cooldown", DEFAULT_COOLDOWN)));
                return true;
            }

            if(this.distributedGameService != null && this.distributedGameService.isRunning()) {
                this.distributedGameService.submitAnswer(snapshot, player, answer);
            }

            return true;
        }

        if(!game.getAnswerOptions().isEmpty()) {
            final String lowerAnswer = answer.toLowerCase();

            if(game.getAnswerOptions().contains(lowerAnswer)) {
                if(this.isOnCooldown(uuid)) {
                    player.sendMessage(MessageUtil.parse(this.configManager.getMessage("cooldown", DEFAULT_COOLDOWN)));
                }

                this.wrongAnswerCooldowns.put(uuid, System.currentTimeMillis());
                return true;
            }
        }

        return false;
    }

    private void endGameWin(final PlatformPlayer winner) {
        if(this.activeGame == null) {
            return;
        }

        this.cancelTimeoutTask();
        this.activeGame.onWin(winner);
        this.clearLocalGameState();
    }

    private void endGameTimeout() {
        if(this.activeGame == null) {
            return;
        }

        this.activeGame.onTimeout();
        this.clearLocalGameState();
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

        if((currentTime - lastAttempt) < cooldownMillis) {
            return true;
        }

        this.wrongAnswerCooldowns.remove(playerId);
        return false;
    }

    private void startDistributedServiceIfNeeded() {
        if(!this.isDistributedModeConfigured()) {
            this.stopDistributedService();
            return;
        }

        if(this.distributedGameService != null && this.distributedGameService.isRunning()) {
            return;
        }

        this.distributedGameService = new DistributedGameService(this.plugin, this, this.configManager.getSettings().redis());
        this.distributedGameService.start();
    }

    private void stopDistributedService() {
        if(this.distributedGameService != null) {
            this.distributedGameService.shutdown();
            this.distributedGameService = null;
        }
    }

    private void clearMatchingDistributedGame(final String gameId) {
        if(this.activeSnapshot == null || !this.activeSnapshot.gameId().equals(gameId)) {
            return;
        }

        this.clearLocalGame();
    }

    private void clearLocalGame() {
        this.cancelTimeoutTask();
        this.clearLocalGameState();
    }

    private void clearLocalGameState() {
        this.activeGame = null;
        this.activeConfig = null;
        this.activeSnapshot = null;
        this.wrongAnswerCooldowns.clear();
    }

    private void runRewards(final GameConfig config, final PlatformPlayer winner) {
        for(final String command : config.getRewardCommands()) {
            final String processed = Templater.process(command, winner);
            this.plugin.platform().dispatchCommand(processed);
        }
    }

    private String plainQuestion(final GameConfig config, final GameSnapshot snapshot) {
        return MessageUtil.plainText(new SharedGame(this.plugin, config, snapshot).getQuestion());
    }

    public Game getActiveGame() {
        return this.activeGame;
    }

}
