package dev.rarehyperion.chatgames.game;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.game.types.ReactionQuestionRenderer;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;

public final class SharedGame extends AbstractGame {

    private final GameSnapshot snapshot;

    public SharedGame(final ChatGamesCore plugin, final GameConfig config, final GameSnapshot snapshot) {
        super(plugin, config, snapshot.type());
        this.snapshot = snapshot;
    }

    @Override
    public void start() {
        this.plugin.broadcast(this.createStartMessage());
    }

    @Override
    public boolean checkAnswer(final String answer) {
        if(this.snapshot.answerMode() == GameSnapshot.AnswerMode.ANY) {
            return true;
        }

        return answer.equalsIgnoreCase(this.snapshot.answer());
    }

    @Override
    public Component getQuestion() {
        if(this.snapshot.type() == GameType.REACTION && this.snapshot.answerMode() == GameSnapshot.AnswerMode.CLICK) {
            return ReactionQuestionRenderer.parse(this.snapshot.question(), this.snapshot.answer());
        }

        return MessageUtil.parse(this.snapshot.question());
    }

    @Override
    public Optional<String> getCorrectAnswer() {
        return this.snapshot.displayAnswer();
    }

    @Override
    public List<String> getAnswerOptions() {
        return this.snapshot.answerOptions();
    }

    public GameSnapshot snapshot() {
        return this.snapshot;
    }

}
