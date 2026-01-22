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

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Folia-specific command implementation using Brigadier.
 * Delegates tab completion to handlers via CommandRegistry.
 *
 * @author RareHyperIon, tannerharkin
 */
@SuppressWarnings({"UnstableApiUsage"})
public class FoliaChatGamesCommand extends ChatGamesCommand {

    public FoliaChatGamesCommand(final ChatGamesCore plugin, final CommandRegistry registry) {
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
                    node.then(this.createStringArgument(subCommand, "game", true));
                    break;
                case TOKEN:
                    node.then(this.createStringArgument(subCommand, "token", false));
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
     * Creates a string argument node that delegates suggestions to the handler.
     */
    private ArgumentBuilder<CommandSourceStack, ?> createStringArgument(
            final SubCommand subCommand,
            final String argName,
            final boolean greedy
    ) {
        return Commands.argument(argName, greedy ? StringArgumentType.greedyString() : StringArgumentType.string())
                .suggests((ctx, builder) -> this.suggestFromHandler(subCommand, ctx, builder))
                .executes(ctx -> {
                    final PlatformSender sender = this.plugin.platform().wrapSender(ctx.getSource().getSender());
                    final String arg = StringArgumentType.getString(ctx, argName);
                    this.handleCommand(sender, new String[]{subCommand.getName(), arg});
                    return 1;
                });
    }

    /**
     * Gets suggestions from the handler's tabComplete method.
     */
    private CompletableFuture<Suggestions> suggestFromHandler(
            final SubCommand subCommand,
            final CommandContext<CommandSourceStack> ctx,
            final SuggestionsBuilder builder
    ) {
        final PlatformSender sender = this.plugin.platform().wrapSender(ctx.getSource().getSender());
        final String partial = builder.getRemaining();
        final List<String> suggestions = this.registry.tabComplete(subCommand, sender, new String[]{partial});

        for (final String suggestion : suggestions) {
            builder.suggest(suggestion);
        }

        return builder.buildFuture();
    }
}
