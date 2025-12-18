package dev.rarehyperion.chatgames.command;

import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.platform.ChatGamesPlugin;
import dev.rarehyperion.chatgames.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ChatGamesCommand implements CommandExecutor, TabCompleter {

    private final ChatGamesPlugin plugin;

    public ChatGamesCommand(final ChatGamesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 0) {
            this.sendHelp(sender);
            return true;
        }

        final String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "reload" -> this.handleReload(sender);
            case "start"  -> this.handleStart(sender, args);
            case "stop"   -> this.handleStop(sender);
            case "list"   -> this.handleList(sender);
            case "info"   -> this.handleInfo(sender);
            case "toggle" -> this.handleToggle(sender);

            default -> {
                this.plugin.sendMessage(sender, MessageUtil.parse("<red>Unknown command. Type /chatgames for help.</red>"));
                yield true;
            }
        };
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            final List<String> subCommands = Arrays.asList("reload", "start", "stop", "list", "toggle", "info");

            return subCommands.stream()
                    .filter(sub -> sender.hasPermission("chatgames." + sub))
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length > 1 && args[0].equalsIgnoreCase("start")) {
            final String partial = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();

            return this.plugin.getGameRegistry().getAllConfigs().stream()
                    .map(GameConfig::getName).filter(Objects::nonNull)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private boolean handleReload(final CommandSender sender) {
        if(!sender.hasPermission("Chatgames.reload")) {
            this.plugin.sendMessage(sender, MessageUtil.parse("<red>You don't have permission to use this command.</red>"));
            return true;
        }

        this.plugin.reload();
        this.plugin.sendMessage(sender, MessageUtil.parse("<green>Successfully reloaded ChatGames!</green>"));
        return true;
    }

    private boolean handleStart(final CommandSender sender, final String[] args) {
        if(!sender.hasPermission("Chatgames.reload")) {
            this.plugin.sendMessage(sender, MessageUtil.parse("<red>You don't have permission to use this command.</red>"));
            return true;
        }

        if (args.length < 2) {
            this.plugin.sendMessage(sender, MessageUtil.parse("<red>Incorrect usage. Usage: /chatgames start <game></red>"));
            return true;
        }

        final String gameName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        this.plugin.getGameRegistry().getConfigByName(gameName).ifPresentOrElse(
                config -> {
                    this.plugin.getGameManager().startGame(config);
                },
                () -> this.plugin.sendMessage(sender, MessageUtil.parse("<red>Unknown game: " + gameName + "</red>"))
        );

        return true;
    }

    private boolean handleStop(final CommandSender sender) {
        if (!sender.hasPermission("chatgames.stop")) {
            this.plugin.sendMessage(sender, MessageUtil.parse("<red>You don't have permission to use this command.</red>"));
            return true;
        }

        this.plugin.getGameManager().stopGame();
        this.plugin.sendMessage(sender, MessageUtil.parse("<green>Game stopped</green>"));
        return true;
    }

    private boolean handleList(final CommandSender sender) {
        if (!sender.hasPermission("chatgames.list")) {
            this.plugin.sendMessage(sender, MessageUtil.parse("<red>You don't have permission to use this command.</red>"));
            return true;
        }

        this.plugin.sendMessage(sender, MessageUtil.parse("<aqua><bold>Available Games:</bold></aqua>"));

        for (final GameConfig config : plugin.getGameRegistry().getAllConfigs()) {
            this.plugin.sendMessage(sender, MessageUtil.parse("<gray>-</gray> <green>" + config.getDisplayName() + "</green>"));
        }

        return true;
    }

    private boolean handleInfo(final CommandSender sender) {
        final String authors = String.join(", ", this.plugin.getDescription().getAuthors());

        this.plugin.sendMessage(sender, MessageUtil.parse("<gold>ChatGames</gold> <yellow>" + this.plugin.getDescription().getVersion() + "</yellow>"));
        this.plugin.sendMessage(sender, MessageUtil.parse("<gray>Platform:</gray> <green>" + this.plugin.getPlatformName() + "</green>"));
        this.plugin.sendMessage(sender, MessageUtil.parse("<gray>Author(s):</gray> <aqua>" + authors + "</aqua>"));
        return true;
    }

    private boolean handleToggle(final CommandSender sender) {
        if (!sender.hasPermission("chatgames.toggle")) {
            this.plugin.sendMessage(sender, MessageUtil.parse("<red>You don't have permission to use this command.</red>"));
            return true;
        }

        boolean current = plugin.getConfig().getBoolean("automatic-games", true);
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

        return true;
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
