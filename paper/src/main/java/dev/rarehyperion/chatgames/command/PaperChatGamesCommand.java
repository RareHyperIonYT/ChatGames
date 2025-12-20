package dev.rarehyperion.chatgames.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.platform.PlatformSender;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"UnstableApiUsage"})
public class PaperChatGamesCommand extends ChatGamesCommand {

    public PaperChatGamesCommand(final ChatGamesCore plugin) {
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
                    final PlatformSender sender = this.plugin.platform().wrapSender(ctx.getSource().getSender());
                    this.handleCommand(sender, new String[]{});
                    return 1;
                });

        final String[][] subCommands = {
                {"reload", "chatgames.reload"},
                {"start", "chatgames.start"},
                {"stop", "chatgames.stop"},
                {"list", "chatgames.list"},
                {"toggle", "chatgames.toggle"},
                {"help", "chatgames.help"},
                {"answer", null},
                {"info", null} // We want info to be accessed by anyone.
        };

        for(final String[] command : subCommands) {
            final String name = command[0];
            final String permission = command[1];

            final LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(name);
            if(permission != null) node.requires(ctx -> ctx.getSender().hasPermission(permission));

            if("start".equals(name)) {
                node.then(this.createArgumentNode(name, "game", StringArgumentType.greedyString()));
            } else if("answer".equals(name)) {
                node.then(this.createArgumentNode(name, "token", StringArgumentType.string()));
            } else {
                node.executes(ctx -> {
                    final PlatformSender sender = this.plugin.platform().wrapSender(ctx.getSource().getSender());
                    this.handleCommand(sender, new String[]{name});
                    return 1;
                });
            }

            root.then(node);
        }

        return root.build();
    }

    private ArgumentBuilder<CommandSourceStack, ?> createArgumentNode(final String command, final String argName, final ArgumentType<?> argType) {
        return Commands.argument(argName, argType)
                .suggests("start".equals(command) ? this::suggestGameNames : (ctx, builder) -> builder.buildFuture())
                .executes(ctx -> {
                    final PlatformSender sender = this.plugin.platform().wrapSender(ctx.getSource().getSender());
                    final String arg = StringArgumentType.getString(ctx, argName);
                    this.handleCommand(sender, new String[]{command, arg});
                    return 1;
                });
    }

    private CompletableFuture<Suggestions> suggestGameNames(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
        for (final GameConfig config : this.plugin.gameRegistry().getAllConfigs()) {
            builder.suggest(config.getName());
        }

        return builder.buildFuture();
    }

}
