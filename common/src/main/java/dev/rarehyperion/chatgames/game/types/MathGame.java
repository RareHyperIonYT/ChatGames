package dev.rarehyperion.chatgames.game.types;

import dev.rarehyperion.chatgames.AbstractChatGames;
import dev.rarehyperion.chatgames.game.AbstractGame;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.game.GameType;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public class MathGame extends AbstractGame {

    private final GameConfig.QuestionAnswer question;

    public MathGame(final AbstractChatGames plugin, final GameConfig config) {
        super(plugin, config, GameType.MATH);
        this.question = this.selectRandom(config.getQuestions());
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


}
