package dev.rarehyperion.chatgames.game.types;

import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ReactionQuestionRenderer {

    private static final Pattern BUTTON_PATTERN = Pattern.compile("<button(?:\\s+([^>]*?))?>(.*?)</button>", Pattern.DOTALL);
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("(\\w+)(?:='([^']*)')?");

    public static Component parse(final String challenge, final String clickToken) {
        final String normalized = normalizeEmojis(challenge);
        final Matcher matcher = BUTTON_PATTERN.matcher(normalized);
        final TextComponent.Builder builder = Component.text();

        int lastEnd = 0;

        while(matcher.find()) {
            if(matcher.start() > lastEnd) {
                builder.append(MessageUtil.parse(normalized.substring(lastEnd, matcher.start())));
            }

            final Map<String, String> attributes = parseAttributes(matcher.group(1));
            Component button = MessageUtil.parse(matcher.group(2));

            if(attributes.containsKey("win")) {
                button = button.clickEvent(ClickEvent.runCommand("/chatgames answer " + clickToken));
            } else {
                button = button.clickEvent(ClickEvent.runCommand("/chatgames answer " + UUID.randomUUID()));
            }

            final String hoverText = attributes.get("hover");
            if(hoverText != null && !hoverText.isEmpty()) {
                button = button.hoverEvent(MessageUtil.parse(hoverText));
            }

            builder.append(button);
            lastEnd = matcher.end();
        }

        if(lastEnd < normalized.length()) {
            builder.append(MessageUtil.parse(normalized.substring(lastEnd)));
        }

        return builder.build();
    }

    public static String normalizeEmojis(final String text) {
        if(text == null) {
            return "";
        }

        return text.replaceAll("\\uFE0F", "");
    }

    private static Map<String, String> parseAttributes(final String attrString) {
        final Map<String, String> map = new HashMap<>();
        if(attrString == null || attrString.trim().isEmpty()) {
            return map;
        }

        final Matcher matcher = ATTRIBUTE_PATTERN.matcher(attrString);

        while(matcher.find()) {
            map.put(matcher.group(1), matcher.group(2));
        }

        return map;
    }

    private ReactionQuestionRenderer() { }

}
