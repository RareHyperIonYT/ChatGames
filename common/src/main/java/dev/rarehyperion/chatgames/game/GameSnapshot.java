package dev.rarehyperion.chatgames.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class GameSnapshot {

    private static final String OPTION_SEPARATOR = "\u001F";

    private final String gameId;
    private final String configName;
    private final GameType type;
    private final String question;
    private final AnswerMode answerMode;
    private final String answer;
    private final List<String> answerOptions;
    private final long startedAtMillis;
    private final long expiresAtMillis;

    public GameSnapshot(final String gameId, final String configName, final GameType type, final String question,
                        final AnswerMode answerMode, final String answer, final List<String> answerOptions,
                        final long startedAtMillis, final long expiresAtMillis) {
        this.gameId = gameId;
        this.configName = configName;
        this.type = type;
        this.question = question;
        this.answerMode = answerMode;
        this.answer = answer == null ? "" : answer;
        this.answerOptions = Collections.unmodifiableList(new ArrayList<>(answerOptions == null ? Collections.emptyList() : answerOptions));
        this.startedAtMillis = startedAtMillis;
        this.expiresAtMillis = expiresAtMillis;
    }

    public static GameSnapshot exact(final String gameId, final String configName, final GameType type,
                                     final String question, final String answer, final List<String> answerOptions,
                                     final long startedAtMillis, final long expiresAtMillis) {
        return new GameSnapshot(gameId, configName, type, question, AnswerMode.EXACT, answer, answerOptions, startedAtMillis, expiresAtMillis);
    }

    public Map<String, String> toRedisHash(final String status) {
        final Map<String, String> values = new LinkedHashMap<>();
        values.put("id", this.gameId);
        values.put("config", this.configName);
        values.put("type", this.type.name());
        values.put("question", this.question);
        values.put("answerMode", this.answerMode.name());
        values.put("answer", this.answer);
        values.put("answerOptions", joinOptions(this.answerOptions));
        values.put("startedAt", String.valueOf(this.startedAtMillis));
        values.put("expiresAt", String.valueOf(this.expiresAtMillis));
        values.put("status", status);
        return values;
    }

    public static GameSnapshot fromRedisHash(final Map<String, String> values) {
        if(values == null || values.isEmpty()) {
            return null;
        }

        final String id = values.get("id");
        final String configName = values.get("config");
        final String typeName = values.get("type");
        final String question = values.get("question");
        final String answerMode = values.get("answerMode");

        if(isBlank(id) || isBlank(configName) || isBlank(typeName) || question == null || isBlank(answerMode)) {
            return null;
        }

        final GameType type = parseType(typeName);
        final AnswerMode parsedAnswerMode = parseAnswerMode(answerMode);
        if(type == null || parsedAnswerMode == null) {
            return null;
        }

        return new GameSnapshot(
                id,
                configName,
                type,
                question,
                parsedAnswerMode,
                values.getOrDefault("answer", ""),
                splitOptions(values.get("answerOptions")),
                parseLong(values.get("startedAt")),
                parseLong(values.get("expiresAt"))
        );
    }

    public String gameId() {
        return this.gameId;
    }

    public String configName() {
        return this.configName;
    }

    public GameType type() {
        return this.type;
    }

    public String question() {
        return this.question;
    }

    public AnswerMode answerMode() {
        return this.answerMode;
    }

    public String answer() {
        return this.answer;
    }

    public List<String> answerOptions() {
        return this.answerOptions;
    }

    public long startedAtMillis() {
        return this.startedAtMillis;
    }

    public long expiresAtMillis() {
        return this.expiresAtMillis;
    }

    public Optional<String> displayAnswer() {
        if(this.answerMode != AnswerMode.EXACT || this.answer.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(this.answer);
    }

    public String normalizedAnswer() {
        return this.answer.toLowerCase(Locale.ROOT);
    }

    private static String joinOptions(final List<String> options) {
        if(options == null || options.isEmpty()) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();

        for(final String option : options) {
            if(builder.length() > 0) {
                builder.append(OPTION_SEPARATOR);
            }

            builder.append(option);
        }

        return builder.toString();
    }

    private static List<String> splitOptions(final String raw) {
        if(raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }

        final String[] parts = raw.split(OPTION_SEPARATOR, -1);
        final List<String> options = new ArrayList<>(parts.length);
        Collections.addAll(options, parts);
        return options;
    }

    private static GameType parseType(final String value) {
        try {
            return GameType.valueOf(value);
        } catch (final IllegalArgumentException ignored) {
            return GameType.fromId(value);
        }
    }

    private static AnswerMode parseAnswerMode(final String value) {
        try {
            return AnswerMode.valueOf(value);
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }

    private static long parseLong(final String value) {
        if(value == null) {
            return 0L;
        }

        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException ignored) {
            return 0L;
        }
    }

    private static boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }

    public enum AnswerMode {
        EXACT,
        ANY,
        CLICK
    }

}
