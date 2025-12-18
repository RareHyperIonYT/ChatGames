package dev.rarehyperion.chatgames.command;

import dev.rarehyperion.chatgames.AbstractChatGames;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class ChatGamesCommand {

    protected final AbstractChatGames plugin;

    public ChatGamesCommand(final AbstractChatGames plugin) {
        this.plugin = plugin;
    }

    public boolean handleCommand(final CommandSender sender, final String[] args) {
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
