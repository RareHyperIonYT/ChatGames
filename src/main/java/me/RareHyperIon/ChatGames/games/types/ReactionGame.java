package me.RareHyperIon.ChatGames.games.types;

import me.RareHyperIon.ChatGames.ChatGames;
import me.RareHyperIon.ChatGames.games.Game;
import me.RareHyperIon.ChatGames.games.GameConfig;
import me.RareHyperIon.ChatGames.handlers.LanguageHandler;
import me.RareHyperIon.ChatGames.utility.Utility;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ReactionGame extends Game {

    private final String word;
    private final GameConfig.ReactionVariant variant;
    private final boolean useVariants;

    public ReactionGame(final ChatGames plugin, final GameConfig config, final LanguageHandler language) {
        super(plugin, config, language);

        // Check if variants are configured
        if (config.reactionVariants != null && !config.reactionVariants.isEmpty()) {
            this.useVariants = true;
            this.variant = config.reactionVariants.get(ThreadLocalRandom.current().nextInt(config.reactionVariants.size()));
            this.word = null;
        } else {
            // Fallback to old words system for backwards compatibility
            this.useVariants = false;
            this.variant = null;
            this.word = config.words.get(ThreadLocalRandom.current().nextInt(config.words.size()));
        }
    }

    @Override
    public void onStart() {
        if (this.plugin.logFull()) {
            this.plugin.getSLF4JLogger().info("Game \"{}\" has started.", this.config.name);
        }

        final String challengeText;
        final String answer;
        if (useVariants) {
            challengeText = this.variant.challenge;
            answer = this.variant.answer;
        } else {
            challengeText = this.word;
            answer = null; // No validation for old word-based system
        }

        final TextComponent messageComponent;

        // If we have an answer to validate, parse buttons and make them individually clickable
        if (answer != null && challengeText.contains("[")) {
            messageComponent = parseClickableButtons(challengeText, answer);
        } else {
            // Fallback: make entire text clickable (old behavior)
            messageComponent = new TextComponent(Utility.color(challengeText));
            messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chatgames-internal-win"));
        }

        for (final Player player : Bukkit.getOnlinePlayers()) {
            String message = this.language.get("ReactionStart")
                .replaceAll("\\{prefix}", this.language.get("Prefix"))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", this.config.displayName)
                .replaceAll("\\{timeout}", String.valueOf(this.config.timeout))
                .replaceAll("\\{descriptor}", this.config.descriptor)
                .replaceAll("\\n", "\n");

            // Add variant name placeholder for custom messages
            if (useVariants) {
                message = message.replaceAll("\\{variant}", this.variant.name);
            }

            final TextComponent finalMessage = new TextComponent(Utility.color(message));
            finalMessage.addExtra(messageComponent);

            player.spigot().sendMessage(finalMessage);
        }
    }

    @Override
    public void onWin(final Player player) {
        if (this.plugin.logFull()) {
            this.plugin.getSLF4JLogger().info("Player \"{}\" has won \"{}\"", player.getName(), this.config.name);
        }

        for (final Player online : Bukkit.getOnlinePlayers()) {
            final String message = this.language.get("ReactionWin")
                .replaceAll("\\{prefix}", this.language.get("Prefix"))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", this.config.displayName)
                .replaceAll("\\{descriptor}", this.config.descriptor)
                .replaceAll("\\n", "\n");

            online.sendMessage(Utility.colorComponent(message, online));
        }

        Bukkit.getScheduler().runTask(this.plugin, () -> {
            for (final String command : this.config.commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("\\{player}", player.getName()).replaceAll("%player%", player.getName()));
            }
        });
    }

    @Override
    public void onEnd() {
        if (this.plugin.logFull()) {
            this.plugin.getSLF4JLogger().info("Game \"{}\" has ended.", this.config.name);
        }

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final String message = this.language.get("ReactionEnd")
                .replaceAll("\\{prefix}", this.language.get("Prefix"))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", this.config.displayName)
                .replaceAll("\\{timeout}", String.valueOf(this.config.timeout))
                .replaceAll("\\{descriptor}", this.config.descriptor)
                .replaceAll("\\n", "\n");

            player.sendMessage(Utility.colorComponent(message, player));
        }
    }

    @Override
    public Map.Entry<String, String> getQuestion() {
        // Return the answer if using variants for validation
        if (useVariants && variant.answer != null) {
            return Map.entry("", variant.answer);
        }
        // Always return null for click-only reactions (old behavior)
        return null;
    }

    /**
     * Parses challenge text and creates separate clickable components for each button.
     * Buttons are expected to be in the format [ButtonText] with optional color codes before them.
     */
    private TextComponent parseClickableButtons(final String text, final String correctAnswer) {
        final TextComponent result = new TextComponent();
        final StringBuilder currentText = new StringBuilder();
        boolean insideBracket = false;
        final StringBuilder buttonText = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);

            if (c == '[') {
                // Capture any color codes immediately before the bracket
                final StringBuilder colorCodes = new StringBuilder();
                int j = currentText.length() - 1;

                // Look backwards for color codes (e.g., &c, &a, &d)
                while (j >= 0) {
                    final char prevChar = currentText.charAt(j);
                    if (j > 0 && currentText.charAt(j - 1) == '&' &&
                        (Character.isLetterOrDigit(prevChar) || prevChar == 'r')) {
                        colorCodes.insert(0, currentText.charAt(j - 1));
                        colorCodes.insert(1, prevChar);
                        j -= 2;
                    } else {
                        break;
                    }
                }

                // Remove color codes from currentText if we found any
                if (colorCodes.length() > 0) {
                    currentText.setLength(currentText.length() - colorCodes.length());
                }

                // Add any remaining text before the bracket as non-clickable
                if (currentText.length() > 0) {
                    result.addExtra(new TextComponent(Utility.color(currentText.toString())));
                    currentText.setLength(0);
                }

                insideBracket = true;
                buttonText.setLength(0);
                buttonText.append(colorCodes); // Include color codes with button
                buttonText.append(c);
            } else if (c == ']' && insideBracket) {
                buttonText.append(c);
                insideBracket = false;

                // Create clickable button component with color codes included
                final String fullButtonWithColor = buttonText.toString(); // e.g., "&c[Red]"
                final TextComponent buttonComponent = new TextComponent(Utility.color(fullButtonWithColor));

                // Extract just the [Text] part for the command (without color codes)
                final String buttonTextOnly = fullButtonWithColor.substring(
                    fullButtonWithColor.indexOf('['));
                buttonComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/chatgames-internal-win " + buttonTextOnly));
                result.addExtra(buttonComponent);
            } else if (insideBracket) {
                buttonText.append(c);
            } else {
                currentText.append(c);
            }
        }

        // Add any remaining text
        if (currentText.length() > 0) {
            result.addExtra(new TextComponent(Utility.color(currentText.toString())));
        }

        return result;
    }

}