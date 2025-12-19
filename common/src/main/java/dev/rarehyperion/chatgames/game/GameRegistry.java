package dev.rarehyperion.chatgames.game;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.game.types.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiFunction;

public final class GameRegistry {

    private final ChatGamesCore plugin;
    private final Map<GameType, BiFunction<ChatGamesCore, GameConfig, Game>> factories = new EnumMap<>(GameType.class);
    private final List<GameConfig> gameConfigs = new ArrayList<>();

    public GameRegistry(final ChatGamesCore plugin) {
        this.plugin = plugin;
    }

    public void registerDefaults() {
        this.registerGameType(GameType.MATH, MathGame::new);
        this.registerGameType(GameType.TRIVIA, TriviaGame::new);
        this.registerGameType(GameType.REACTION, ReactionGame::new);
        this.registerGameType(GameType.UNSCRAMBLE, UnscrambleGame::new);
        this.registerGameType(GameType.MULTIPLE_CHOICE, MultipleChoiceGame::new);
    }

    public void registerGameType(final GameType type, final BiFunction<ChatGamesCore, GameConfig, Game> factory) {
        this.factories.put(type, factory);
    }

    public void loadGames() {
        this.gameConfigs.clear();

        final File gamesFolder = new File(this.plugin.platform().getDataFolder(), "games");

        if(!gamesFolder.exists()) {
            this.createDefaultGames(gamesFolder);
        }

        final File[] gameFiles = gamesFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if(gameFiles == null || gameFiles.length == 0) {
            this.plugin.platform().getLogger().warning("No game configurations were found!");
            return;
        }

        for(final File file : gameFiles) {
            try {
                final GameConfig config = new GameConfig(this.plugin.platform().loadConfig(file));

                if(config.getType() != null) {
                    this.gameConfigs.add(config);
                } else {
                    this.plugin.platform().getLogger().warning("Invalid game type in: " + file.getName());
                }
            } catch (final Exception exception) {
                this.plugin.platform().getLogger().severe("Failed to load game: " + file.getName());
                exception.printStackTrace(System.err);
            }
        }

        this.plugin.platform().getLogger().info("Loaded " + this.gameConfigs.size() + " game configuration(s)");
    }

    public Game createGame(final GameConfig config) {
        final BiFunction<ChatGamesCore, GameConfig, Game> factory = this.factories.get(config.getType());

        if(factory == null) {
            throw new IllegalStateException("No factory registered for game type: " + config.getType());
        }

        return factory.apply(this.plugin, config);
    }

    public Optional<GameConfig> getRandomConfig() {
        if (this.gameConfigs.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(this.gameConfigs.get(new Random().nextInt(this.gameConfigs.size())));
    }

    public Optional<GameConfig> getConfigByName(final String name) {
        return this.gameConfigs.stream()
                .filter(config -> {
                    assert config.getName() != null;
                    return config.getName().equalsIgnoreCase(name);
                })
                .findFirst();
    }

    public List<GameConfig> getAllConfigs() {
        return Collections.unmodifiableList(this.gameConfigs);
    }

    private void createDefaultGames(final File folder) {
        if(!folder.exists() && !folder.mkdirs()) throw new IllegalStateException("Unable to create games folder.");
        final String[] defaults = { "math.yml", "trivia.yml", "unscramble.yml", "reaction.yml", "multiple-choice.yml" };

        for (String fileName : defaults) {
            this.saveResource("games/" + fileName, folder);
        }
    }

    private void saveResource(final String resourcePath, final File folder) {
        try (final InputStream stream = this.plugin.platform().getResource(resourcePath)) {
            if (stream != null) {
                File output = new File(folder, new File(resourcePath).getName());
                Files.copy(stream, output.toPath());
            }
        } catch (final IOException e) {
            this.plugin.platform().getLogger().severe("Failed to save resource: " + resourcePath);
        }
    }

}