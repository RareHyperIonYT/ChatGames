package dev.rarehyperion.chatgames.game.types;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.game.AbstractGame;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.game.GameType;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;

import java.util.*;

public class UnscrambleGame extends AbstractGame {

    private final GameConfig.QuestionAnswer question;

    public UnscrambleGame(final ChatGamesCore plugin, final GameConfig config) {
        super(plugin, config, GameType.TRIVIA);
        this.question = this.getScramble(config.getWords());
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

    private GameConfig.QuestionAnswer getScramble(final List<String> options) {
        final Random random = new Random();
        final String answer = options.get(new Random().nextInt(options.size()));

        final List<Character> characters = new ArrayList<>();
        for(char character : answer.toCharArray()) characters.add(character);
        Collections.shuffle(characters, random);

        final StringBuilder scrambled = new StringBuilder(characters.size());
        for(char character : characters) scrambled.append(character);

        return new GameConfig.QuestionAnswer(scrambled.toString(), answer);
    }

}
