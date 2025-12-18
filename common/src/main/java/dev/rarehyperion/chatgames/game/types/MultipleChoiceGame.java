package dev.rarehyperion.chatgames.game.types;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.game.AbstractGame;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.game.GameType;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MultipleChoiceGame extends AbstractGame {

    private static final Pattern OPTION_PATTERN = Pattern.compile("^([A-H])\\.");

    private final GameConfig.MultipleChoiceQuestion question;
    private final List<String> answerOptions;

    public MultipleChoiceGame(final ChatGamesCore plugin, final GameConfig config) {
        super(plugin, config, GameType.TRIVIA);
        this.question = this.selectRandom(config.getMultipleChoiceQuestions());
        this.answerOptions = extractAnswerOptions(this.question.answers());
    }

    @Override
    public void start() {
        this.plugin.broadcast(this.createStartMessage());
    }

    @Override
    public boolean checkAnswer(final String answer) {
        return answer.equalsIgnoreCase(this.question.correctAnswer());
    }

    @Override
    public Component getQuestion() {
        final String fullQuestion = this.question.question() + "\n" + String.join("\n", question.answers());
        return MessageUtil.parse(fullQuestion);
    }

    @Override
    public List<String> getAnswerOptions() {
        return this.answerOptions;
    }

    @Override
    public Optional<String> getCorrectAnswer() {
        return Optional.of(this.question.correctAnswer());
    }

    private List<String> extractAnswerOptions(final List<String> answers) {
        return answers.stream()
                .map(answer -> {
                    final Matcher matcher = OPTION_PATTERN.matcher(answer);

                    if(matcher.find()) {
                        return matcher.group(1).toLowerCase();
                    }

                    return answer.trim().toLowerCase();
                }).collect(Collectors.toList());
    }

}
