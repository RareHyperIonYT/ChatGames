package dev.rarehyperion.chatgames.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.platform.ChatGamesPlugin;
import dev.rarehyperion.chatgames.util.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;

public class ChatGamesCommand {

    private final ChatGamesPlugin plugin;

    public ChatGamesCommand(final ChatGamesPlugin plugin) {
        this.plugin = plugin;
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
        return Commands.literal("chatgames")
                .executes(this::executeHelp)

                .then(Commands.literal("reload")
                        .requires(src -> src.getSender().hasPermission("chatgames.reload"))
                        .executes(this::handleReload)
                )

                .then(Commands.literal("start")
                        .requires(src -> src.getSender().hasPermission("chatgames.list"))
                        .then(Commands.argument("game", StringArgumentType.string())
                                .suggests(this::suggestGameNames)
                                .executes(this::handleStart)
                        )
                )

                .then(Commands.literal("stop")
                        .requires(src -> src.getSender().hasPermission("chatgames.stop"))
                        .executes(this::handleStop)
                )

                .then(Commands.literal("list")
                        .requires(src -> src.getSender().hasPermission("chatgames.list"))
                        .executes(this::handleList)
                )

                .then(Commands.literal("toggle")
                        .requires(src -> src.getSender().hasPermission("chatgames.toggle"))
                        .executes(this::handleToggle)
                )

                .then(Commands.literal("info")
                        .executes(this::handleInfo)
                ).build();
    }

    private int handleReload(final CommandContext<CommandSourceStack> ctx) {
        final CommandSender sender = ctx.getSource().getSender();

        this.plugin.reload();
        this.plugin.sendMessage(sender, MessageUtil.parse("<green>Successfully reloaded ChatGames!</green>"));
        return 1;
    }

    private int handleStart(final CommandContext<CommandSourceStack> ctx) {
        final CommandSender sender = ctx.getSource().getSender();
        final String gameName = StringArgumentType.getString(ctx, "game");

        this.plugin.getGameRegistry().getConfigByName(gameName).ifPresentOrElse(
                config -> this.plugin.getGameManager().startGame(config),
                () -> this.plugin.sendMessage(sender, MessageUtil.parse("<red>Unknown game: " + gameName + "</red>"))
        );

        return 1;
    }

    private int handleStop(final CommandContext<CommandSourceStack> ctx) {
        final CommandSender sender = ctx.getSource().getSender();

        this.plugin.getGameManager().stopGame();
        this.plugin.sendMessage(sender, MessageUtil.parse("<green>Game stopped</green>"));
        return 1;
    }

    private int handleList(final CommandContext<CommandSourceStack> ctx) {
        final CommandSender sender = ctx.getSource().getSender();

        this.plugin.sendMessage(sender, MessageUtil.parse("<aqua><bold>Available Games:</bold></aqua>"));

        for (final GameConfig config : plugin.getGameRegistry().getAllConfigs()) {
            this.plugin.sendMessage(sender, MessageUtil.parse(
                    "<gray>-</gray> <green>" + config.getDisplayName() + "</green>"
            ));
        }

        return 1;
    }

    private int handleInfo(final CommandContext<CommandSourceStack> ctx) {
        final CommandSender sender = ctx.getSource().getSender();

        final String authors = String.join(", ", this.plugin.getDescription().getAuthors());

        this.plugin.sendMessage(sender, MessageUtil.parse("<gold>ChatGames</gold> <yellow>" + this.plugin.getDescription().getVersion() + "</yellow>"));
        this.plugin.sendMessage(sender, MessageUtil.parse("<gray>Platform:</gray> <green>" + this.plugin.getPlatformName() + "</green>"));
        this.plugin.sendMessage(sender, MessageUtil.parse("<gray>Author(s):</gray> <aqua>" + authors + "</aqua>"));

        return 1;
    }

    private int handleToggle(final CommandContext<CommandSourceStack> ctx) {
        final CommandSender sender = ctx.getSource().getSender();

        boolean current = this.plugin.getConfig().getBoolean("automatic-games", true);
        boolean newValue = !current;

        this.plugin.getConfig().set("automatic-games", newValue);
        this.plugin.saveConfig();
        this.plugin.getConfigManager().load();

        if (newValue) {
            this.plugin.getGameManager().startScheduler();
            this.plugin.sendMessage(sender, MessageUtil.parse("<green>Automatic games enabled!</green>"));
        } else {
            this.plugin.getGameManager().shutdown();
            this.plugin.sendMessage(sender, MessageUtil.parse("<red>Automatic games disabled!</red>"));
        }

        return 1;
    }

    private int executeHelp(final CommandContext<CommandSourceStack> ctx) {
        sendHelp(ctx.getSource().getSender());
        return 1;
    }

    private CompletableFuture<Suggestions> suggestGameNames(
            final CommandContext<CommandSourceStack> ctx,
            final SuggestionsBuilder builder
    ) {
        // Use game registry to suggest names
        for (final GameConfig config : this.plugin.getGameRegistry().getAllConfigs()) {
            builder.suggest(config.getName());
        }
        return builder.buildFuture();
    }

    private void sendHelp(final CommandSender sender) {
        this.plugin.sendMessage(sender, MessageUtil.parse("<gray><bold>ChatGames Commands:</bold></gray>"));
        this.plugin.sendMessage(sender, MessageUtil.parse("<yellow>/chatgames reload</yellow> <gray>- Reloads the plugin</gray>"));
        this.plugin.sendMessage(sender, MessageUtil.parse("<yellow>/chatgames start <game></yellow> <gray>- Starts the specified game</gray>"));
        this.plugin.sendMessage(sender, MessageUtil.parse("<yellow>/chatgames stop</yellow> <gray>- Stop the current game</gray>"));
        this.plugin.sendMessage(sender, MessageUtil.parse("<yellow>/chatgames list</yellow> <gray>- Lists all available games</gray>"));
        this.plugin.sendMessage(sender, MessageUtil.parse("<yellow>/chatgames toggle</yellow> <gray>- Toggles automatic games</gray>"));
        this.plugin.sendMessage(sender, MessageUtil.parse("<yellow>/chatgames info</yellow> <gray>- Displays plugin information</gray>"));
    }

}
