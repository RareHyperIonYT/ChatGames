package dev.rarehyperion.chatgames.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.platform.PlatformSender;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.concurrent.CompletableFuture;

/**
 * Paper-specific command implementation using Brigadier.
 * Builds command tree from SubCommand enum for single source of truth.
 *
 * @author RareHyperIon, tannerharkin
 */
@SuppressWarnings({"UnstableApiUsage"})
public class PaperChatGamesCommand extends ChatGamesCommand {

    public PaperChatGamesCommand(final ChatGamesCore plugin, final CommandRegistry registry) {
        super(plugin, registry);
    }

    /**
     * Builds the full command tree from SubCommand enum.
     *
     * @return The root command node.
     */
    public LiteralCommandNode<CommandSourceStack> build() {
        final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("chatgames")
                .executes((ctx) -> {
                    final PlatformSender sender = this.plugin.platform().wrapSender(ctx.getSource().getSender());
                    this.handleCommand(sender, new String[]{});
                    return 1;
                });

        // Build subcommands from SubCommand enum
        for (final SubCommand subCommand : SubCommand.values()) {
            final LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(subCommand.getName());

            // Add permission requirement if needed
            if (subCommand.requiresPermission()) {
                node.requires(ctx -> ctx.getSender().hasPermission(subCommand.getPermission()));
            }

            // Handle argument types
            switch (subCommand.getArgumentType()) {
                case GAME_NAME:
                    node.then(this.createGameArgument(subCommand.getName()));
                    break;
                case TOKEN:
                    node.then(this.createTokenArgument(subCommand.getName()));
                    break;
                case NONE:
                default:
                    node.executes(ctx -> {
                        final PlatformSender sender = this.plugin.platform().wrapSender(ctx.getSource().getSender());
                        this.handleCommand(sender, new String[]{subCommand.getName()});
                        return 1;
                    });
                    break;
            }

            root.then(node);
        }

        return root.build();
    }

    /**
     * Creates an argument node for game name arguments with suggestions.
     */
    private ArgumentBuilder<CommandSourceStack, ?> createGameArgument(final String commandName) {
        return Commands.argument("game", StringArgumentType.greedyString())
                .suggests(this::suggestGameNames)
                .executes(ctx -> {
                    final PlatformSender sender = this.plugin.platform().wrapSender(ctx.getSource().getSender());
                    final String game = StringArgumentType.getString(ctx, "game");
                    this.handleCommand(sender, new String[]{commandName, game});
                    return 1;
                });
    }

    /**
     * Creates an argument node for token arguments (no suggestions).
     */
    private ArgumentBuilder<CommandSourceStack, ?> createTokenArgument(final String commandName) {
        return Commands.argument("token", StringArgumentType.string())
                .executes(ctx -> {
                    final PlatformSender sender = this.plugin.platform().wrapSender(ctx.getSource().getSender());
                    final String token = StringArgumentType.getString(ctx, "token");
                    this.handleCommand(sender, new String[]{commandName, token});
                    return 1;
                });
    }

    /**
     * Provides game name suggestions for tab completion.
     */
    private CompletableFuture<Suggestions> suggestGameNames(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
        for (final String gameName : this.registry.getGameNames()) {
            builder.suggest(gameName);
        }
        return builder.buildFuture();
    }
}
