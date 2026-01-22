package dev.rarehyperion.chatgames.command;

import com.mojang.brigadier.arguments.StringArgumentType;
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
 * Paper-specific command implementation using Brigadier.
 * Delegates tab completion and permissions to handlers via CommandRegistry.
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
     * Permission checks and argument handling are delegated to handlers.
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
            final SubCommandHandler handler = this.registry.getHandler(subCommand);
            final LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(subCommand.getName());

            // Add permission requirement from handler if needed
            final String permission = handler != null ? handler.getPermission() : null;
            if (permission != null) {
                node.requires(ctx -> ctx.getSender().hasPermission(permission));
            }

            // Execute without arguments
            node.executes(ctx -> {
                final PlatformSender sender = this.plugin.platform().wrapSender(ctx.getSource().getSender());
                this.handleCommand(sender, new String[]{subCommand.getName()});
                return 1;
            });

            // Add optional greedy argument for commands that may need arguments
            // The handler will validate and use arguments as needed
            node.then(Commands.argument("args", StringArgumentType.greedyString())
                    .suggests((ctx, builder) -> this.suggestFromHandler(subCommand, ctx, builder))
                    .executes(ctx -> {
                        final PlatformSender sender = this.plugin.platform().wrapSender(ctx.getSource().getSender());
                        final String args = StringArgumentType.getString(ctx, "args");
                        this.handleCommand(sender, new String[]{subCommand.getName(), args});
                        return 1;
                    }));

            root.then(node);
        }

        return root.build();
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
