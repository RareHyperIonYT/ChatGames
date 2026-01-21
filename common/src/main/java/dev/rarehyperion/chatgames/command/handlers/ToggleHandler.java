package dev.rarehyperion.chatgames.command.handlers;

import dev.rarehyperion.chatgames.command.CommandContext;
import dev.rarehyperion.chatgames.command.SubCommand;
import dev.rarehyperion.chatgames.command.SubCommandHandler;
import dev.rarehyperion.chatgames.util.MessageUtil;

/**
 * Handler for the toggle subcommand.
 * Toggles automatic games on/off.
 *
 * @author RareHyperIon, tannerharkin
 */
public class ToggleHandler implements SubCommandHandler {

    private static final String NO_PERMISSION_DEFAULT = "<red>You don't have permission to use this command.</red>";

    @Override
    public void execute(final CommandContext context) {
        final String permission = SubCommand.TOGGLE.getPermission();

        if (!context.hasPermission(permission)) {
            context.getSender().sendMessage(MessageUtil.parse(
                    context.getPlugin().configManager().getMessage("permission", NO_PERMISSION_DEFAULT)
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
            context.getSender().sendMessage(MessageUtil.parse("<green>Automatic games enabled!</green>"));
        } else {
            context.getPlugin().gameManager().shutdown();
            context.getSender().sendMessage(MessageUtil.parse("<red>Automatic games disabled!</red>"));
        }
    }
}
