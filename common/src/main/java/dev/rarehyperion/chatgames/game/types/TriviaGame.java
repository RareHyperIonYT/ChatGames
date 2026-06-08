package dev.rarehyperion.chatgames.game.types;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.game.AbstractGame;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.game.GameSnapshot;
import dev.rarehyperion.chatgames.game.GameType;
import dev.rarehyperion.chatgames.game.SnapshotSource;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.Optional;

public class TriviaGame extends AbstractGame implements SnapshotSource {

    private final GameConfig.QuestionAnswer question;

    public TriviaGame(final ChatGamesCore plugin, final GameConfig config) {
        super(plugin, config, GameType.TRIVIA);
        this.question = this.config.nextQuestion();
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

    @Override
    public GameSnapshot createSnapshot(final String gameId, final long startedAtMillis, final long expiresAtMillis) {
        return GameSnapshot.exact(
                gameId,
                this.config.getName(),
                GameType.TRIVIA,
                this.question.question(),
                this.question.answer(),
                Collections.emptyList(),
                startedAtMillis,
                expiresAtMillis
        );
    }

}
