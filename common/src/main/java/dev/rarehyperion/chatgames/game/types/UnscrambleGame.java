package dev.rarehyperion.chatgames.game.types;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.game.AbstractGame;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.game.GameSnapshot;
import dev.rarehyperion.chatgames.game.GameType;
import dev.rarehyperion.chatgames.game.SnapshotSource;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class UnscrambleGame extends AbstractGame implements SnapshotSource {

    private final GameConfig.QuestionAnswer question;

    public UnscrambleGame(final ChatGamesCore plugin, final GameConfig config) {
        super(plugin, config, GameType.UNSCRAMBLE);
        this.question = this.buildScramble(this.config.nextWord());
    }
    
    @Override
    public void start() {
        this.plugin.broadcast(this.createStartMessage());
    }

    @Override
    public boolean checkAnswer(final String answer) {
        return answer.equalsIgnoreCase(this.question.answer());
    }

    @Override
    public Component getQuestion() {
        return MessageUtil.parse(this.question.question());
    }

    @Override
    public Optional<String> getCorrectAnswer() {
        return Optional.of(this.question.answer());
    }

    private GameConfig.QuestionAnswer buildScramble(final String word) {
        final List<Character> characters = new ArrayList<>();
        for (final char c : word.toCharArray()) characters.add(c);
        Collections.shuffle(characters, ThreadLocalRandom.current());

        final StringBuilder scrambled = new StringBuilder(characters.size());
        for (final char c : characters) scrambled.append(c);

        return new GameConfig.QuestionAnswer(scrambled.toString(), word);
    }

    @Override
    public GameSnapshot createSnapshot(final String gameId, final long startedAtMillis, final long expiresAtMillis) {
        return GameSnapshot.exact(
                gameId,
                this.config.getName(),
                GameType.UNSCRAMBLE,
                this.question.question(),
                this.question.answer(),
                Collections.emptyList(),
                startedAtMillis,
                expiresAtMillis
        );
    }

}
