package dev.rarehyperion.chatgames.redis;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.config.ConfigManager.RedisSettings;
import dev.rarehyperion.chatgames.game.Game;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.game.GameManager;
import dev.rarehyperion.chatgames.game.GameSnapshot;
import dev.rarehyperion.chatgames.game.SnapshotSource;
import dev.rarehyperion.chatgames.platform.PlatformPlayer;
import dev.rarehyperion.chatgames.platform.PlatformTask;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.params.SetParams;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class DistributedGameService {

    private static final long STATE_CLEANUP_MILLIS = TimeUnit.MINUTES.toMillis(2);
    private static final long SERVER_PRESENCE_MILLIS = TimeUnit.SECONDS.toMillis(30);
    private static final long REDIS_ERROR_LOG_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(30);
    private static final long FOLLOWER_HEARTBEAT_TICKS = 200L;

    private static final String LEADER_SCRIPT =
            "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
            "redis.call('PEXPIRE', KEYS[1], ARGV[2]); return 1; end; " +
            "local ok = redis.call('SET', KEYS[1], ARGV[1], 'NX', 'PX', ARGV[2]); " +
            "if ok then return 1; end; return 0;";

    private static final String START_SCRIPT =
            "local status = redis.call('HGET', KEYS[1], 'status'); " +
            "local expiresAt = tonumber(redis.call('HGET', KEYS[1], 'expiresAt') or '0'); " +
            "local now = tonumber(ARGV[1]); " +
            "if status == 'active' and expiresAt > now then return 0; end; " +
            "redis.call('DEL', KEYS[1]); " +
            "for i = 4, #ARGV, 2 do redis.call('HSET', KEYS[1], ARGV[i], ARGV[i + 1]); end; " +
            "redis.call('PEXPIRE', KEYS[1], ARGV[2]); " +
            "redis.call('PUBLISH', KEYS[2], ARGV[3]); " +
            "return 1;";

    private static final String ANSWER_SCRIPT =
            "if redis.call('HGET', KEYS[1], 'status') ~= 'active' then return 0; end; " +
            "if redis.call('HGET', KEYS[1], 'id') ~= ARGV[1] then return 0; end; " +
            "if redis.call('HGET', KEYS[1], 'winnerUuid') then return 0; end; " +
            "local now = tonumber(ARGV[5]); " +
            "local expiresAt = tonumber(redis.call('HGET', KEYS[1], 'expiresAt') or '0'); " +
            "if expiresAt > 0 and now > expiresAt then return 0; end; " +
            "local mode = redis.call('HGET', KEYS[1], 'answerMode') or 'EXACT'; " +
            "local expected = redis.call('HGET', KEYS[1], 'answer') or ''; " +
            "local submitted = ARGV[2] or ''; " +
            "local correct = 0; " +
            "if mode == 'ANY' then correct = 1; " +
            "elseif string.lower(submitted) == string.lower(expected) then correct = 1; end; " +
            "if correct == 0 then return 0; end; " +
            "redis.call('HSET', KEYS[1], 'status', 'ended', 'winnerUuid', ARGV[3], 'winnerName', ARGV[4], 'endedAt', ARGV[5], 'endReason', 'WIN'); " +
            "redis.call('PEXPIRE', KEYS[1], ARGV[6]); " +
            "redis.call('PUBLISH', KEYS[2], ARGV[7]); " +
            "return 1;";

    private static final String TIMEOUT_SCRIPT =
            "if redis.call('HGET', KEYS[1], 'status') ~= 'active' then return 0; end; " +
            "if redis.call('HGET', KEYS[1], 'id') ~= ARGV[1] then return 0; end; " +
            "local expiresAt = tonumber(redis.call('HGET', KEYS[1], 'expiresAt') or '0'); " +
            "local now = tonumber(ARGV[2]); " +
            "if expiresAt <= 0 or now < expiresAt then return 0; end; " +
            "redis.call('HSET', KEYS[1], 'status', 'ended', 'endedAt', ARGV[2], 'endReason', 'TIMEOUT'); " +
            "redis.call('PEXPIRE', KEYS[1], ARGV[3]); " +
            "redis.call('PUBLISH', KEYS[2], ARGV[4]); " +
            "return 1;";

    private static final String STOP_SCRIPT =
            "if redis.call('HGET', KEYS[1], 'status') ~= 'active' then return 0; end; " +
            "if redis.call('HGET', KEYS[1], 'id') ~= ARGV[1] then return 0; end; " +
            "redis.call('HSET', KEYS[1], 'status', 'ended', 'endedAt', ARGV[2], 'endReason', 'COMMAND'); " +
            "redis.call('PEXPIRE', KEYS[1], ARGV[3]); " +
            "redis.call('PUBLISH', KEYS[2], ARGV[4]); " +
            "return 1;";

    private final ChatGamesCore plugin;
    private final GameManager gameManager;
    private final RedisSettings settings;
    private final String serverId;
    private final String keyPrefix;
    private final long leaderLockMillis;
    private final long rewardDedupMillis;

    private JedisPool pool;
    private ExecutorService executor;
    private Thread subscriberThread;
    private JedisPubSub subscriber;
    private PlatformTask heartbeatTask;

    private volatile boolean running;
    private volatile boolean leader;
    private volatile boolean connected;
    private volatile long lastRedisErrorLog;

    public DistributedGameService(final ChatGamesCore plugin, final GameManager gameManager, final RedisSettings settings) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.settings = settings;
        this.serverId = plugin.platform().name().toLowerCase() + "-" + UUID.randomUUID();
        this.keyPrefix = normalizePrefix(settings.keyPrefix());
        this.leaderLockMillis = Math.max(TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(settings.leaderLockSeconds()));
        this.rewardDedupMillis = Math.max(TimeUnit.MINUTES.toMillis(5), TimeUnit.SECONDS.toMillis(settings.rewardDedupSeconds()));
    }

    public void start() {
        if(this.running) {
            return;
        }

        this.running = true;
        this.executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("ChatGames-Redis"));
        this.pool = this.createPool();
        this.startSubscriber();

        final long heartbeatTicks = this.plugin.configManager().getSettings().distributedAutomaticLeader()
                ? Math.max(20L, Math.min(100L, (this.leaderLockMillis / 1000L) * 20L / 3L))
                : FOLLOWER_HEARTBEAT_TICKS;
        this.heartbeatTask = this.plugin.platform().runTaskTimer(new Runnable() {
            @Override
            public void run() {
                executeAsync(new RedisWork() {
                    @Override
                    public void run() {
                        heartbeat();
                    }
                });
            }
        }, 20L, heartbeatTicks);

        this.executeAsync(new RedisWork() {
            @Override
            public void run() {
                syncCurrent(false);
            }
        });

        this.plugin.platform().getLogger().info("Redis multi-server mode enabled. Server id: " + this.serverId);
    }

    public void shutdown() {
        this.running = false;
        this.leader = false;
        this.connected = false;

        if(this.heartbeatTask != null) {
            this.heartbeatTask.cancel();
            this.heartbeatTask = null;
        }

        if(this.subscriber != null) {
            try {
                this.subscriber.unsubscribe();
            } catch (final Exception ignored) { }
            this.subscriber = null;
        }

        if(this.executor != null) {
            this.executor.shutdownNow();
            this.executor = null;
        }

        if(this.pool != null) {
            this.pool.close();
            this.pool = null;
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean isLeader() {
        return this.leader;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void requestAutomaticStart(final GameConfig config, final int minimumPlayers) {
        this.executeAsync(new RedisWork() {
            @Override
            public void run() {
                if(!plugin.configManager().getSettings().distributedAutomaticLeader()) {
                    return;
                }

                try (final Jedis jedis = pool.getResource()) {
                    markConnected();
                    updateServerPresence(jedis);

                    if(!ensureLeader(jedis)) {
                        return;
                    }

                    if(getClusterOnlinePlayers(jedis) < minimumPlayers) {
                        return;
                    }

                    startGame(jedis, config);
                } catch (final Exception exception) {
                    logRedisFailure("automatic start", exception);
                }
            }
        });
    }

    public void requestManualStart(final GameConfig config) {
        this.executeAsync(new RedisWork() {
            @Override
            public void run() {
                try (final Jedis jedis = pool.getResource()) {
                    markConnected();
                    updateServerPresence(jedis);
                    startGame(jedis, config);
                } catch (final Exception exception) {
                    logRedisFailure("manual start", exception);
                }
            }
        });
    }

    public void requestStop() {
        this.executeAsync(new RedisWork() {
            @Override
            public void run() {
                try (final Jedis jedis = pool.getResource()) {
                    markConnected();
                    final GameSnapshot snapshot = readCurrentSnapshot(jedis);
                    if(snapshot == null) {
                        return;
                    }

                    final String event = DistributedEvent.stop(serverId, snapshot.gameId()).encode();
                    jedis.eval(
                            STOP_SCRIPT,
                            Arrays.asList(currentKey(), eventsChannel()),
                            Arrays.asList(snapshot.gameId(), String.valueOf(System.currentTimeMillis()), String.valueOf(STATE_CLEANUP_MILLIS), event)
                    );
                } catch (final Exception exception) {
                    logRedisFailure("stop", exception);
                }
            }
        });
    }

    public void submitAnswer(final GameSnapshot snapshot, final PlatformPlayer player, final String answer) {
        final UUID winnerId = player.id();
        final String winnerName = player.name();

        this.executeAsync(new RedisWork() {
            @Override
            public void run() {
                try (final Jedis jedis = pool.getResource()) {
                    markConnected();
                    final String event = DistributedEvent.win(serverId, snapshot.gameId(), winnerId, winnerName).encode();
                    final Object result = jedis.eval(
                            ANSWER_SCRIPT,
                            Arrays.asList(currentKey(), eventsChannel()),
                            Arrays.asList(
                                    snapshot.gameId(),
                                    answer,
                                    winnerId.toString(),
                                    winnerName,
                                    String.valueOf(System.currentTimeMillis()),
                                    String.valueOf(STATE_CLEANUP_MILLIS),
                                    event
                            )
                    );

                    if(isLuaSuccess(result)) {
                        final GameSnapshot latest = readCurrentSnapshot(jedis);
                        final boolean rewardClaimed = claimReward(jedis, snapshot.gameId());
                        gameManager.handleDistributedWin(latest == null ? snapshot : latest, winnerId, winnerName, rewardClaimed);
                    }
                } catch (final Exception exception) {
                    logRedisFailure("answer submit", exception);
                }
            }
        });
    }

    private JedisPool createPool() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(4);
        poolConfig.setMaxIdle(2);
        poolConfig.setMinIdle(0);
        poolConfig.setTestOnBorrow(true);

        final DefaultJedisClientConfig.Builder clientConfig = DefaultJedisClientConfig.builder()
                .connectionTimeoutMillis(this.settings.timeoutMillis())
                .socketTimeoutMillis(this.settings.timeoutMillis())
                .database(this.settings.database())
                .ssl(this.settings.ssl())
                .clientName("ChatGames-" + this.serverId);

        if(!isBlank(this.settings.username())) {
            clientConfig.user(this.settings.username());
        }

        if(!isBlank(this.settings.password())) {
            clientConfig.password(this.settings.password());
        }

        return new JedisPool(poolConfig, new HostAndPort(this.settings.host(), this.settings.port()), clientConfig.build());
    }

    private void startSubscriber() {
        this.subscriber = new JedisPubSub() {
            @Override
            public void onMessage(final String channel, final String message) {
                final DistributedEvent event = DistributedEvent.decode(message);
                if(event == null) {
                    return;
                }

                executeAsync(new RedisWork() {
                    @Override
                    public void run() {
                        handleEvent(event);
                    }
                });
            }
        };

        this.subscriberThread = new Thread(new Runnable() {
            @Override
            public void run() {
                subscribeLoop();
            }
        }, "ChatGames-Redis-Subscriber");
        this.subscriberThread.setDaemon(true);
        this.subscriberThread.start();
    }

    private void subscribeLoop() {
        while(this.running) {
            try (final Jedis jedis = this.pool.getResource()) {
                this.markConnected();
                jedis.subscribe(this.subscriber, this.eventsChannel());
            } catch (final Exception exception) {
                if(this.running) {
                    this.logRedisFailure("pubsub subscribe", exception);
                    sleepQuietly(2000L);
                }
            }
        }
    }

    private void heartbeat() {
        try (final Jedis jedis = this.pool.getResource()) {
            this.markConnected();
            this.updateServerPresence(jedis);
            this.leader = this.plugin.configManager().getSettings().distributedAutomaticLeader() && this.ensureLeader(jedis);

            this.syncAndTimeoutActiveGame(jedis);
        } catch (final Exception exception) {
            this.leader = false;
            this.logRedisFailure("heartbeat", exception);
        }
    }

    private void syncAndTimeoutActiveGame(final Jedis jedis) {
        final GameSnapshot snapshot = this.readCurrentSnapshot(jedis);
        if(snapshot == null || !this.isActive(jedis)) {
            return;
        }

        final long now = System.currentTimeMillis();

        if(snapshot.expiresAtMillis() > now && !this.gameManager.isDistributedGameActive(snapshot.gameId())) {
            this.gameManager.activateDistributedGame(snapshot, false);
            return;
        }

        if(snapshot.expiresAtMillis() <= now) {
            this.timeoutExpiredGame(jedis, snapshot);
        }
    }

    private void syncCurrent(final boolean announce) {
        try (final Jedis jedis = this.pool.getResource()) {
            this.markConnected();
            final GameSnapshot snapshot = this.readCurrentSnapshot(jedis);
            if(snapshot != null && this.isActive(jedis) && snapshot.expiresAtMillis() > System.currentTimeMillis()) {
                this.gameManager.activateDistributedGame(snapshot, announce);
            }
        } catch (final Exception exception) {
            this.logRedisFailure("sync current", exception);
        }
    }

    private void startGame(final Jedis jedis, final GameConfig config) {
        final Game game = this.plugin.gameRegistry().createGame(config);

        if(!(game instanceof SnapshotSource)) {
            this.plugin.platform().getLogger().warn("Cannot start distributed game '" + config.getName() + "' because it cannot export a snapshot.");
            return;
        }

        final long now = System.currentTimeMillis();
        final long expiresAt = now + TimeUnit.SECONDS.toMillis(config.getTimeoutSeconds());
        final GameSnapshot snapshot = ((SnapshotSource) game).createSnapshot(UUID.randomUUID().toString(), now, expiresAt);
        final Map<String, String> hash = snapshot.toRedisHash("active");
        final List<String> args = new ArrayList<>();
        args.add(String.valueOf(now));
        args.add(String.valueOf(config.getTimeoutSeconds() * 1000L + STATE_CLEANUP_MILLIS));
        args.add(DistributedEvent.start(this.serverId, snapshot.gameId()).encode());

        for(final Map.Entry<String, String> entry : hash.entrySet()) {
            args.add(entry.getKey());
            args.add(entry.getValue());
        }

        final Object result = jedis.eval(START_SCRIPT, Arrays.asList(this.currentKey(), this.eventsChannel()), args);
        if(isLuaSuccess(result)) {
            this.gameManager.activateDistributedGame(snapshot, true);
        } else if(this.plugin.configManager().getSettings().debug()) {
            this.plugin.platform().getLogger().info("Distributed game start skipped because a game is already active.");
        }
    }

    private void timeoutExpiredGame(final Jedis jedis, final GameSnapshot snapshot) {
        final String event = DistributedEvent.timeout(this.serverId, snapshot.gameId()).encode();
        jedis.eval(
                TIMEOUT_SCRIPT,
                Arrays.asList(this.currentKey(), this.eventsChannel()),
                Arrays.asList(snapshot.gameId(), String.valueOf(System.currentTimeMillis()), String.valueOf(STATE_CLEANUP_MILLIS), event)
        );
    }

    private void handleEvent(final DistributedEvent event) {
        try (final Jedis jedis = this.pool.getResource()) {
            this.markConnected();
            final GameSnapshot snapshot = this.readCurrentSnapshot(jedis);
            if(snapshot == null || !snapshot.gameId().equals(event.gameId)) {
                return;
            }

            if("start".equals(event.type)) {
                if(this.isActive(jedis)) {
                    this.gameManager.activateDistributedGame(snapshot, true);
                }
                return;
            }

            if("win".equals(event.type)) {
                final boolean shouldRewardHere = this.plugin.platform().getOnlinePlayers().contains(event.winnerId);
                final boolean rewardClaimed = shouldRewardHere && this.claimReward(jedis, event.gameId);
                this.gameManager.handleDistributedWin(snapshot, event.winnerId, event.winnerName, rewardClaimed);
                return;
            }

            if("timeout".equals(event.type)) {
                this.gameManager.handleDistributedTimeout(snapshot);
                return;
            }

            if("stop".equals(event.type)) {
                this.gameManager.handleDistributedStop(snapshot);
            }
        } catch (final Exception exception) {
            this.logRedisFailure("event handling", exception);
        }
    }

    private boolean ensureLeader(final Jedis jedis) {
        final Object result = jedis.eval(
                LEADER_SCRIPT,
                Collections.singletonList(this.leaderKey()),
                Arrays.asList(this.serverId, String.valueOf(this.leaderLockMillis))
        );
        return isLuaSuccess(result);
    }

    private void updateServerPresence(final Jedis jedis) {
        final long now = System.currentTimeMillis();
        jedis.set(this.serverPlayersKey(this.serverId), String.valueOf(this.plugin.platform().getOnlinePlayers().size()), SetParams.setParams().px(SERVER_PRESENCE_MILLIS));
        jedis.zadd(this.serversKey(), now, this.serverId);
        jedis.zremrangeByScore(this.serversKey(), 0, now - SERVER_PRESENCE_MILLIS);
    }

    private int getClusterOnlinePlayers(final Jedis jedis) {
        final long now = System.currentTimeMillis();
        final List<String> serverIds = jedis.zrangeByScore(this.serversKey(), now - SERVER_PRESENCE_MILLIS, now + 1000L);
        int online = 0;

        for(final String id : serverIds) {
            final String raw = jedis.get(this.serverPlayersKey(id));
            if(raw == null) {
                continue;
            }

            try {
                online += Integer.parseInt(raw);
            } catch (final NumberFormatException ignored) { }
        }

        return online;
    }

    private boolean claimReward(final Jedis jedis, final String gameId) {
        final String result = jedis.set(this.rewardKey(gameId), this.serverId, SetParams.setParams().nx().px(this.rewardDedupMillis));
        return "OK".equalsIgnoreCase(result);
    }

    private GameSnapshot readCurrentSnapshot(final Jedis jedis) {
        return GameSnapshot.fromRedisHash(jedis.hgetAll(this.currentKey()));
    }

    private boolean isActive(final Jedis jedis) {
        return "active".equalsIgnoreCase(jedis.hget(this.currentKey(), "status"));
    }

    private void executeAsync(final RedisWork work) {
        final ExecutorService currentExecutor = this.executor;
        if(!this.running || currentExecutor == null) {
            return;
        }

        currentExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(!running) {
                    return;
                }

                work.run();
            }
        });
    }

    private void markConnected() {
        this.connected = true;
    }

    private void logRedisFailure(final String action, final Exception exception) {
        this.connected = false;
        final long now = System.currentTimeMillis();

        if(now - this.lastRedisErrorLog < REDIS_ERROR_LOG_INTERVAL_MILLIS) {
            return;
        }

        this.lastRedisErrorLog = now;
        this.plugin.platform().getLogger().warn("Redis " + action + " failed: " + exception.getMessage());
    }

    private String currentKey() {
        return this.keyPrefix + ":current";
    }

    private String leaderKey() {
        return this.keyPrefix + ":leader";
    }

    private String eventsChannel() {
        return this.keyPrefix + ":events";
    }

    private String serversKey() {
        return this.keyPrefix + ":servers";
    }

    private String serverPlayersKey(final String id) {
        return this.keyPrefix + ":server:" + id + ":players";
    }

    private String rewardKey(final String gameId) {
        return this.keyPrefix + ":reward:" + gameId;
    }

    private static boolean isLuaSuccess(final Object result) {
        return result instanceof Number && ((Number) result).longValue() == 1L;
    }

    private static String normalizePrefix(final String rawPrefix) {
        String prefix = isBlank(rawPrefix) ? "chatgame" : rawPrefix.trim();

        while(prefix.endsWith(":")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }

        return prefix.isEmpty() ? "chatgame" : prefix;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void sleepQuietly(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private interface RedisWork {

        void run();

    }

    private static final class NamedThreadFactory implements ThreadFactory {

        private final String name;

        private NamedThreadFactory(final String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(final Runnable runnable) {
            final Thread thread = new Thread(runnable, this.name);
            thread.setDaemon(true);
            return thread;
        }

    }

    private static final class DistributedEvent {

        private final String type;
        private final String source;
        private final String gameId;
        private final UUID winnerId;
        private final String winnerName;

        private DistributedEvent(final String type, final String source, final String gameId, final UUID winnerId, final String winnerName) {
            this.type = type;
            this.source = source;
            this.gameId = gameId;
            this.winnerId = winnerId;
            this.winnerName = winnerName;
        }

        private static DistributedEvent start(final String source, final String gameId) {
            return new DistributedEvent("start", source, gameId, null, null);
        }

        private static DistributedEvent win(final String source, final String gameId, final UUID winnerId, final String winnerName) {
            return new DistributedEvent("win", source, gameId, winnerId, winnerName);
        }

        private static DistributedEvent timeout(final String source, final String gameId) {
            return new DistributedEvent("timeout", source, gameId, null, null);
        }

        private static DistributedEvent stop(final String source, final String gameId) {
            return new DistributedEvent("stop", source, gameId, null, null);
        }

        private String encode() {
            final StringBuilder builder = new StringBuilder();
            builder.append(this.type).append('|').append(this.source).append('|').append(this.gameId);

            if(this.winnerId != null) {
                builder.append('|').append(this.winnerId).append('|').append(encodeString(this.winnerName));
            }

            return builder.toString();
        }

        private static DistributedEvent decode(final String raw) {
            if(raw == null) {
                return null;
            }

            final String[] parts = raw.split("\\|", -1);
            if(parts.length < 3) {
                return null;
            }

            if("win".equals(parts[0])) {
                if(parts.length < 5) {
                    return null;
                }

                try {
                    return win(parts[1], parts[2], UUID.fromString(parts[3]), decodeString(parts[4]));
                } catch (final IllegalArgumentException ignored) {
                    return null;
                }
            }

            if("start".equals(parts[0])) {
                return start(parts[1], parts[2]);
            }

            if("timeout".equals(parts[0])) {
                return timeout(parts[1], parts[2]);
            }

            if("stop".equals(parts[0])) {
                return stop(parts[1], parts[2]);
            }

            return null;
        }

        private static String encodeString(final String value) {
            return Base64.getUrlEncoder().withoutPadding().encodeToString((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
        }

        private static String decodeString(final String value) {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        }

    }

}
