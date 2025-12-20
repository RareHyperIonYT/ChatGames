package dev.rarehyperion.chatgames.command;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.platform.PlatformSender;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpongeChatGamesCommand extends ChatGamesCommand {

    public SpongeChatGamesCommand(final ChatGamesCore plugin) {
        super(plugin);
    }

    public Command.Parameterized build() {
        return Command.builder()
                .executor(context -> {
                    final PlatformSender sender = this.plugin.platform().wrapSender(context.cause().first(Player.class).orElse(context.cause().first(Player.class).orElse(null)));
                    this.handleCommand(sender, new String[]{});
                    return CommandResult.success();
                })
                .addChild(reloadCommand(), "reload")
                .addChild(startCommand(), "start")
                .addChild(stopCommand(), "stop")
                .addChild(listCommand(), "list")
                .addChild(toggleCommand(), "toggle")
                .addChild(infoCommand(), "info")
                .addChild(answerCommand(), "answer")
                .addChild(helpCommand(), "help")
                .build();
    }

    private Command.Parameterized reloadCommand() {
        return Command.builder()
                .executor(ctx -> {
                    PlatformSender sender = this.plugin.platform().wrapSender(ctx.cause().first(Player.class).orElse(null));
                    handleCommand(sender, new String[]{"reload"});
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized startCommand() {
        Parameter.Value<String> gameParam = Parameter.remainingJoinedStrings()
                .key("game")
                .completer((context, string) -> {
                    if (context.hasPermission("chatgames.start")) {
                        final String partial = string.toLowerCase();

                        return this.plugin.gameRegistry().getAllConfigs().stream()
                                .map(GameConfig::getName).filter(Objects::nonNull)
                                .filter(name -> name.toLowerCase().startsWith(partial))
                                .map(CommandCompletion::of)
                                .collect(Collectors.toList());
                    }

                    return Collections.emptyList();
                })
                .build();

        return Command.builder()
                .addParameter(gameParam)
                .executor(ctx -> {
                    PlatformSender sender = this.plugin.platform().wrapSender(ctx.cause().first(Player.class).orElse(null));
                    String game = ctx.requireOne(gameParam);
                    handleCommand(sender, new String[]{"start", game});
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized stopCommand() {
        return Command.builder()
                .executor(ctx -> {
                    PlatformSender sender = this.plugin.platform().wrapSender(ctx.cause().first(Player.class).orElse(null));
                    handleCommand(sender, new String[]{"stop"});
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized listCommand() {
        return Command.builder()
                .executor(ctx -> {
                    PlatformSender sender = this.plugin.platform().wrapSender(ctx.cause().first(Player.class).orElse(null));
                    handleCommand(sender, new String[]{"list"});
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized toggleCommand() {
        return Command.builder()
                .executor(ctx -> {
                    PlatformSender sender = this.plugin.platform().wrapSender(ctx.cause().first(Player.class).orElse(null));
                    handleCommand(sender, new String[]{"toggle"});
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized infoCommand() {
        return Command.builder()
                .executor(ctx -> {
                    PlatformSender sender = this.plugin.platform().wrapSender(ctx.cause().first(Player.class).orElse(null));
                    handleCommand(sender, new String[]{"info"});
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized answerCommand() {
        Parameter.Value<String> tokenParam = Parameter.string()
                .key("token")
                .build();

        return Command.builder()
                .addParameter(tokenParam)
                .executor(ctx -> {
                    PlatformSender sender = this.plugin.platform().wrapSender(ctx.cause().first(Player.class).orElse(null));
                    String token = ctx.requireOne(tokenParam);
                    handleCommand(sender, new String[]{"answer", token});
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized helpCommand() {
        return Command.builder()
                .executor(ctx -> {
                    PlatformSender sender = this.plugin.platform().wrapSender(ctx.cause().first(Player.class).orElse(null));
                    handleCommand(sender, new String[]{});
                    return CommandResult.success();
                })
                .build();
    }

}
