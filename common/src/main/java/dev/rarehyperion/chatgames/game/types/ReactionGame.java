package dev.rarehyperion.chatgames.game.types;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.game.AbstractGame;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.game.GameType;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReactionGame extends AbstractGame {

    private static final Pattern BUTTON_PATTERN = Pattern.compile("<button(?:\\s+hover='([^']*)')?>(.*?)</button>");

    private final GameConfig.ReactionVariant variant;
    public final String clickToken;

    public ReactionGame(final ChatGamesCore plugin, final GameConfig config) {
        super(plugin, config, GameType.TRIVIA);
        this.variant = this.selectRandom(config.getReactionVariants());
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
        final String challenge = this.variant.challenge();

        if ("click".equalsIgnoreCase(this.variant.answer())) {
            return this.parseButton(challenge);
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

    private Component parseButton(final String challenge) {
        final Matcher matcher = BUTTON_PATTERN.matcher(challenge);
        final TextComponent.Builder builder = Component.text();

        int lastEnd = 0;

        while(matcher.find()) {
            if(matcher.start() > lastEnd) {
                final String beforeText = challenge.substring(lastEnd, matcher.start());
                builder.append(MessageUtil.parse(beforeText));
            }

            final String hoverText = matcher.group(1);
            final String buttonText = matcher.group(2);

            Component button = MessageUtil.parse(buttonText)
                    .clickEvent(ClickEvent.runCommand("/chatgames answer " + this.clickToken));

            if(hoverText != null && !hoverText.isEmpty()) {
                button = button.hoverEvent(MessageUtil.parse(hoverText));
            }

            builder.append(button);
            lastEnd = matcher.end();
        }

        if(lastEnd < challenge.length()) {
            final String afterText = challenge.substring(lastEnd);
            builder.append(MessageUtil.parse(afterText));
        }

        return builder.build();
    }
    
}
