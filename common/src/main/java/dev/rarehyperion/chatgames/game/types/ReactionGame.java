package dev.rarehyperion.chatgames.game.types;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.game.AbstractGame;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.game.GameType;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public class ReactionGame extends AbstractGame {

    private final GameConfig.ReactionVariant variant;

    public ReactionGame(final ChatGamesCore plugin, final GameConfig config) {
        super(plugin, config, GameType.TRIVIA);
        this.variant = this.selectRandom(config.getReactionVariants());
    }
    
    @Override
    public void start() {
        this.plugin.broadcast(this.createStartMessage());
    }

    @Override
    public boolean checkAnswer(final String answer) {
        if(this.variant.answer().isEmpty()) return true;
        return answer.equalsIgnoreCase(this.variant.answer());
    }

    @Override
    public Component getQuestion() {
        return MessageUtil.parse(this.variant.challenge());
    }

    @Override
    public Optional<String> getCorrectAnswer() {
        return this.variant.answer().isEmpty() ?
                Optional.empty() :
                Optional.of(this.variant.answer());
    }
    
}
