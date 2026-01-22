package dev.rarehyperion.chatgames.command;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.platform.PlatformSender;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Sponge-specific command implementation.
 * Delegates tab completion and permissions to handlers via CommandRegistry.
 *
 * @author RareHyperIon, tannerharkin
 */
public class SpongeChatGamesCommand extends ChatGamesCommand {

    public SpongeChatGamesCommand(final ChatGamesCore plugin, final CommandRegistry registry) {
        super(plugin, registry);
    }

    /**
     * Builds the full command tree from SubCommand enum.
     * Permission checks and argument handling are delegated to handlers.
     *
     * @return The root command.
     */
    public Command.Parameterized build() {
        final Command.Builder builder = Command.builder()
                .executor(context -> {
                    final PlatformSender sender = this.wrapCause(context);
                    this.handleCommand(sender, new String[]{});
                    return CommandResult.success();
                });

        // Build subcommands from SubCommand enum
        for (final SubCommand subCommand : SubCommand.values()) {
            builder.addChild(this.buildSubCommand(subCommand), subCommand.getName());
        }

        return builder.build();
    }

    /**
     * Builds a subcommand from the SubCommand enum entry.
     * Gets permission from the handler.
     */
    private Command.Parameterized buildSubCommand(final SubCommand subCommand) {
        final Command.Builder builder = Command.builder();
        final SubCommandHandler handler = this.registry.getHandler(subCommand);

        // Add permission requirement from handler if needed
        final String permission = handler != null ? handler.getPermission() : null;
        if (permission != null) {
            builder.permission(permission);
        }

        // Create optional argument parameter for commands that may need arguments
        final Parameter.Value<String> argsParam = Parameter.remainingJoinedStrings()
                .key("args")
                .optional()
                .completer((context, partial) -> {
                    final PlatformSender sender = this.wrapCause(context);
                    final List<String> suggestions = this.registry.tabComplete(
                            subCommand, sender, new String[]{partial}
                    );
                    return suggestions.stream()
                            .map(CommandCompletion::of)
                            .collect(Collectors.toList());
                })
                .build();

        builder.addParameter(argsParam)
                .executor(context -> {
                    final PlatformSender sender = this.wrapCause(context);
                    final String args = context.one(argsParam).orElse(null);
                    if (args != null) {
                        this.handleCommand(sender, new String[]{subCommand.getName(), args});
                    } else {
                        this.handleCommand(sender, new String[]{subCommand.getName()});
                    }
                    return CommandResult.success();
                });

        return builder.build();
    }

    /**
     * Wraps the command cause to a PlatformSender.
     * Handles both player and console senders.
     */
    private PlatformSender wrapCause(final org.spongepowered.api.command.parameter.CommandContext context) {
        final Object sender = context.cause().first(ServerPlayer.class)
                .map(p -> (Object) p)
                .orElse(null);
        return this.plugin.platform().wrapSender(sender);
    }
}
