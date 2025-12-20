package dev.rarehyperion.chatgames.command;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.platform.PlatformSender;
import dev.rarehyperion.chatgames.util.MessageUtil;

import java.util.Arrays;

public class ChatGamesCommand {

    protected final ChatGamesCore plugin;
    protected final ConfigManager configManager;

    public ChatGamesCommand(final ChatGamesCore plugin) {
        this.plugin = plugin;
        this.configManager = plugin.configManager();
    }

    public boolean handleCommand(final PlatformSender sender, final String[] args) {
        if(args.length == 0) {
            this.sendHelp(sender);
            return true;
        }

        final String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> this.handleReload(sender);
            case "start"  -> this.handleStart(sender, args);
            case "stop"   -> this.handleStop(sender);
            case "list"   -> this.handleList(sender);
            case "info"   -> this.handleInfo(sender);
            case "toggle" -> this.handleToggle(sender);
            case "help"   -> this.sendHelp(sender);
            default -> sender.sendMessage(MessageUtil.parse("<red>Unknown command. Type /chatgames for help.</red>"));
        };

        return true;
    }

    private void handleReload(final PlatformSender sender) {
        if(!sender.hasPermission("Chatgames.reload")) {
            sender.sendMessage(MessageUtil.parse(
                    this.configManager.getMessage("permission", "<red>You don't have permission to use this command.</red>")
            ));
            return;
        }

        this.plugin.reload();

        sender.sendMessage(MessageUtil.parse(
                this.configManager.getMessage("reload", "<green>Successfully reloaded ChatGames!</green>")
        ));
    }

    private void handleStart(final PlatformSender sender, final String[] args) {
        if(!sender.hasPermission("Chatgames.reload")) {
            sender.sendMessage(MessageUtil.parse(
                    this.configManager.getMessage("permission", "<red>You don't have permission to use this command.</red>")
            ));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(MessageUtil.parse("<red>Incorrect usage. Usage: /chatgames start <game></red>"));
            return;
        }

        final String gameName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        this.plugin.gameRegistry().getConfigByName(gameName).ifPresentOrElse(
                config -> {
                    this.plugin.gameManager().startGame(config);
                },
                () -> sender.sendMessage(MessageUtil.parse("<red>Unknown game: " + gameName + "</red>"))
        );
    }

    private void handleStop(final PlatformSender sender) {
        if (!sender.hasPermission("chatgames.stop")) {
            sender.sendMessage(MessageUtil.parse(
                    this.configManager.getMessage("permission", "<red>You don't have permission to use this command.</red>")
            ));
            return;
        }

        this.plugin.gameManager().stopGame();
        sender.sendMessage(MessageUtil.parse("<green>Game stopped</green>"));
    }

    private void handleList(final PlatformSender sender) {
        if (!sender.hasPermission("chatgames.list")) {
            sender.sendMessage(MessageUtil.parse(
                    this.configManager.getMessage("permission", "<red>You don't have permission to use this command.</red>")
            ));
            return;
        }

        sender.sendMessage(MessageUtil.parse("<aqua><bold>Available Games:</bold></aqua>"));

        for (final GameConfig config : plugin.gameRegistry().getAllConfigs()) {
            sender.sendMessage(MessageUtil.parse("<gray>-</gray> <green>" + config.getDisplayName() + "</green>"));
        }
    }

    private void handleInfo(final PlatformSender sender) {
        final String authors = String.join(", and ", this.plugin.platform().pluginMeta().getAuthors());

        final String response = """
        <gold>ChatGames <white>•</white> <yellow>%s</yellow></gold>
        <green>A simple plugin that adds chat-based games <aqua>by %s</aqua></green>
        """;

        sender.sendMessage(MessageUtil.parse(
                String.format(response, this.plugin.platform().pluginMeta().getVersion(), authors)
        ));
    }

    private void handleToggle(final PlatformSender sender) {
        if (!sender.hasPermission("chatgames.toggle")) {
            sender.sendMessage(MessageUtil.parse(
                    this.configManager.getMessage("permission", "<red>You don't have permission to use this command.</red>")
            ));
            return;
        }

        boolean current = this.plugin.platform().getConfigValue("automatic-games", Boolean.class, true);
        boolean newValue = !current;

        this.plugin.platform().setConfigValue("automatic-games", newValue);
        this.plugin.platform().saveConfig();
        this.plugin.configManager().load();

        if (newValue) {
            this.plugin.gameManager().startScheduler();
            sender.sendMessage(MessageUtil.parse("<green>Automatic games enabled!</green>"));
        } else {
            this.plugin.gameManager().shutdown();
            sender.sendMessage(MessageUtil.parse("<red>Automatic games disabled!</red>"));
        }
    }

    private void sendHelp(final PlatformSender sender) {
        sender.sendMessage(MessageUtil.parse("<gray><bold>ChatGames Commands:</bold></gray>"));
        sender.sendMessage(MessageUtil.parse("<yellow>/chatgames reload</yellow> <gray>- Reloads the plugin</gray>"));
        sender.sendMessage(MessageUtil.parse("<yellow>/chatgames start <game></yellow> <gray>- Starts the specified game</gray>"));
        sender.sendMessage(MessageUtil.parse("<yellow>/chatgames stop</yellow> <gray>- Stop the current game</gray>"));
        sender.sendMessage(MessageUtil.parse("<yellow>/chatgames list</yellow> <gray>- Lists all available games</gray>"));
        sender.sendMessage(MessageUtil.parse("<yellow>/chatgames toggle</yellow> <gray>- Toggles automatic games</gray>"));
        sender.sendMessage(MessageUtil.parse("<yellow>/chatgames info</yellow> <gray>- Displays plugin information</gray>"));
    }

}
