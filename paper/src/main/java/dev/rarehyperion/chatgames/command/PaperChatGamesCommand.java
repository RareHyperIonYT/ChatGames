package dev.rarehyperion.chatgames.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.rarehyperion.chatgames.AbstractChatGames;
import dev.rarehyperion.chatgames.game.GameConfig;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"UnstableApiUsage"})
public class PaperChatGamesCommand extends ChatGamesCommand {

    public PaperChatGamesCommand(final AbstractChatGames plugin) {
        super(plugin);
    }

    /**
     * Building the full command tree:
     * /chatgames
     *   reload
     *   start <game>
     *   stop
     *   list
     *   toggle
     *   info
     */
    public LiteralCommandNode<CommandSourceStack> build() {
        final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("chatgames")
                .executes((ctx) -> {
                    this.handleCommand(ctx.getSource().getSender(), new String[]{});
                    return 1;
                });

        final String[][] subCommands = {
                {"reload", "chatgames.reload"},
                {"start", "chatgames.start"},
                {"stop", "chatgames.stop"},
                {"list", "chatgames.list"},
                {"toggle", "chatgames.toggle"},
                {"info", null} // We want info to be accessed by anyone.
        };

        for(final String[] command : subCommands) {
            final String name = command[0];
            final String permission = command[1];

            final LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(name);
            if(permission != null) node.requires(ctx -> ctx.getSender().hasPermission(permission));

            if("start".equals(name)) {
                node.then(Commands.argument("game", StringArgumentType.greedyString())
                        .suggests(this::suggestGameNames)
                        .executes(ctx -> {
                            final String game = StringArgumentType.getString(ctx, "game");
                            this.handleCommand(ctx.getSource().getSender(), new String[]{"start", game});
                            return 1;
                        }));
            } else {
                node.executes(ctx -> {
                    this.handleCommand(ctx.getSource().getSender(), new String[]{name});
                    return 1;
                });
            }

            root.then(node);
        }

        return root.build();
    }

    private CompletableFuture<Suggestions> suggestGameNames(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
        for (final GameConfig config : this.plugin.getGameRegistry().getAllConfigs()) {
            builder.suggest(config.getName());
        }

        return builder.buildFuture();
    }

}
