package me.RareHyperIon.ChatGames.games;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameConfig {

    public final String name, descriptor;
    public final List<String> commands;
    public final int timeout;

    public final List<MultipleChoiceQuestion> multipleChoiceQuestions;
    public final List<Map.Entry<String, String>> choices;
    public final List<String> words;
    public final List<ReactionVariant> reactionVariants;

    public GameConfig(final FileConfiguration configuration) {
        this.name = configuration.getString("name");
        this.descriptor = configuration.getString("descriptor");
        this.commands = configuration.getStringList("reward-commands");
        this.timeout = configuration.getInt("timeout");

        this.multipleChoiceQuestions = this.parseMultipleChoiceQuestions(configuration.getConfigurationSection("questions"));
        this.choices = this.parseChoices(configuration.getList("questions"));
        this.words = configuration.getStringList("words");
        this.reactionVariants = this.parseReactionVariants(configuration.getList("variants"));
    }

    private List<MultipleChoiceQuestion> parseMultipleChoiceQuestions(final ConfigurationSection section) {
        final List<MultipleChoiceQuestion> questions = new ArrayList<>();

        if (section == null) return questions;

        for (final String key : section.getKeys(false)) {
            final ConfigurationSection questionSection = section.getConfigurationSection(key);
            if (questionSection == null) continue;

            final String question = questionSection.getString("question");
            final List<String> answers = questionSection.getStringList("answers");
            final String correctAnswer = questionSection.getString("correct-answer");

            if (question == null || answers.isEmpty() || correctAnswer == null) continue;

            questions.add(new MultipleChoiceQuestion(question, answers, correctAnswer));
        }

        return questions;
    }

    private List<Map.Entry<String, String>> parseChoices(final List<?> list) {
        final List<Map.Entry<String, String>> choices = new ArrayList<>();

        if(list == null) return choices;

        for(final Object object : list) {
            if(!(object instanceof List<?> choice)) continue;
            if(choice.size() < 2) continue;

            choices.add(new AbstractMap.SimpleEntry<>((String) choice.get(0), (String) choice.get(1)));
        }

        return choices;
    }

    private List<ReactionVariant> parseReactionVariants(final List<?> list) {
        final List<ReactionVariant> variants = new ArrayList<>();

        if(list == null) return variants;

        for(final Object object : list) {
            if(!(object instanceof Map<?, ?> variantMap)) continue;

            final String variantName = (String) variantMap.get("name");
            final String challenge = (String) variantMap.get("challenge");
            final String answer = (String) variantMap.get("answer");

            if(variantName == null || challenge == null || answer == null) continue;

            variants.add(new ReactionVariant(variantName, challenge, answer));
        }

        return variants;
    }

    public static class MultipleChoiceQuestion {
        public final String question;
        public final List<String> answers;
        public final String correctAnswer;

        public MultipleChoiceQuestion(final String question, final List<String> answers, final String correctAnswer) {
            this.question = question;
            this.answers = answers;
            this.correctAnswer = correctAnswer;
        }
    }

    public static class ReactionVariant {
        public final String name;
        public final String challenge;
        public final String answer;

        public ReactionVariant(final String name, final String challenge, final String answer) {
            this.name = name;
            this.challenge = challenge;
            this.answer = answer;
        }
    }

}