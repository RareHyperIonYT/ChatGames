package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.config.ConfigManager;
import dev.rarehyperion.chatgames.util.MessageUtil;

/**
 * Handler for the toggle subcommand.
 * Toggles automatic games on/off.
 *
 * @author RareHyperIon, tannerharkin
 */
public class ToggleHandler implements SubCommandHandler {

    private static final String DEFAULT_NO_PERMISSION = "<red>You don't have permission to use this command.</red>";
    private static final String DEFAULT_ENABLED = "<green>Automatic games enabled!</green>";
    private static final String DEFAULT_DISABLED = "<red>Automatic games disabled!</red>";

    @Override
    public String getPermission() {
        return "chatgames.toggle";
    }

    @Override
    public String getUsage() {
        return "/chatgames toggle";
    }

    @Override
    public String getDescription() {
        return "Toggles automatic games on/off";
    }

    @Override
    public void execute(final CommandContext context) {
        final ConfigManager configManager = context.getPlugin().configManager();

        if (!hasPermission(context)) {
            context.getSender().sendMessage(MessageUtil.parse(
                    configManager.getMessage("permission", DEFAULT_NO_PERMISSION)
            ));
            return;
        }

        final boolean current = context.getPlugin().platform().getConfigValue("automatic-games", Boolean.class, true);
        final boolean newValue = !current;

        context.getPlugin().platform().setConfigValue("automatic-games", newValue);
        context.getPlugin().platform().saveConfig();
        context.getPlugin().configManager().load();

        if (newValue) {
            context.getPlugin().gameManager().startScheduler();
            context.getSender().sendMessage(MessageUtil.parse(
                    configManager.getMessage("toggle-enabled", DEFAULT_ENABLED)
            ));
        } else {
            context.getPlugin().gameManager().shutdown();
            context.getSender().sendMessage(MessageUtil.parse(
                    configManager.getMessage("toggle-disabled", DEFAULT_DISABLED)
            ));
        }
    }
}
