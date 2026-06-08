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
import java.util.UUID;

public class ReactionGame extends AbstractGame implements SnapshotSource {

    private final GameConfig.ReactionVariant variant;
    public final String clickToken;

    public ReactionGame(final ChatGamesCore plugin, final GameConfig config) {
        super(plugin, config, GameType.REACTION);
        this.variant = this.config.nextVariant();
        this.clickToken = UUID.randomUUID().toString();
    }
    
    @Override
    public void start() {
        this.plugin.broadcast(this.createStartMessage());
    }

    @Override
    public boolean checkAnswer(final String answer) {
        if("click".equalsIgnoreCase(this.variant.answer())) {
            return answer.equalsIgnoreCase(this.clickToken);
        }

        if(this.variant.answer().isEmpty()) return true;
        return answer.equalsIgnoreCase(this.variant.answer());
    }

    @Override
    public Component getQuestion() {
        final String challenge = ReactionQuestionRenderer.normalizeEmojis(this.variant.challenge());

        if ("click".equalsIgnoreCase(this.variant.answer())) {
            return ReactionQuestionRenderer.parse(challenge, this.clickToken);
        }

        return MessageUtil.parse(challenge);
    }

    @Override
    public Optional<String> getCorrectAnswer() {
        if(this.variant.answer().isEmpty() || "click".equalsIgnoreCase(this.variant.answer())) {
            return Optional.empty();
        }

        return Optional.of(this.variant.answer());
    }

    @Override
    public GameSnapshot createSnapshot(final String gameId, final long startedAtMillis, final long expiresAtMillis) {
        final String variantAnswer = this.variant.answer();

        if(variantAnswer.isEmpty()) {
            return new GameSnapshot(
                    gameId,
                    this.config.getName(),
                    GameType.REACTION,
                    ReactionQuestionRenderer.normalizeEmojis(this.variant.challenge()),
                    GameSnapshot.AnswerMode.ANY,
                    "",
                    Collections.emptyList(),
                    startedAtMillis,
                    expiresAtMillis
            );
        }

        if("click".equalsIgnoreCase(variantAnswer)) {
            return new GameSnapshot(
                    gameId,
                    this.config.getName(),
                    GameType.REACTION,
                    ReactionQuestionRenderer.normalizeEmojis(this.variant.challenge()),
                    GameSnapshot.AnswerMode.CLICK,
                    this.clickToken,
                    Collections.emptyList(),
                    startedAtMillis,
                    expiresAtMillis
            );
        }

        return GameSnapshot.exact(
                gameId,
                this.config.getName(),
                GameType.REACTION,
                ReactionQuestionRenderer.normalizeEmojis(this.variant.challenge()),
                variantAnswer,
                Collections.emptyList(),
                startedAtMillis,
                expiresAtMillis
        );
    }

}
