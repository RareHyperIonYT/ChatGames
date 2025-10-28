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
        if (useVariants) {
            challengeText = this.variant.challenge;
        } else {
            challengeText = this.word;
        }

        final TextComponent messageComponent = new TextComponent(Utility.color(challengeText));
        messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chatgames-internal-win"));

        for (final Player player : Bukkit.getOnlinePlayers()) {
            String message = this.language.get("ReactionStart")
                .replaceAll("\\{prefix}", this.language.get("Prefix"))
                .replaceAll("\\{player}", player.getName())
                .replaceAll("\\{name}", this.config.name)
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
                .replaceAll("\\{name}", this.config.name)
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
                .replaceAll("\\{name}", this.config.name)
                .replaceAll("\\{timeout}", String.valueOf(this.config.timeout))
                .replaceAll("\\{descriptor}", this.config.descriptor)
                .replaceAll("\\n", "\n");

            player.sendMessage(Utility.colorComponent(message, player));
        }
    }

    @Override
    public Map.Entry<String, String> getQuestion() {
        // Always return null for click-only reactions
        return null;
    }

}
