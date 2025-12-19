package dev.rarehyperion.chatgames.command;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.platform.PlatformSender;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;

public class SpongeChatGamesCommand extends ChatGamesCommand {

    public SpongeChatGamesCommand(final ChatGamesCore plugin) {
        super(plugin);
    }

    public Command.Parameterized build() {
        return Command.builder()
                .executor(context -> {
                    PlatformSender sender = this.plugin.platform().wrapSender(context.cause().first(Player.class).orElse(context.cause().first(Player.class).orElse(null)));
                    handleCommand(sender, new String[]{});
                    return CommandResult.success();
                })
                .addChild(reloadCommand(), "reload")
                .addChild(startCommand(), "start")
                .addChild(stopCommand(), "stop")
                .addChild(listCommand(), "list")
                .addChild(toggleCommand(), "toggle")
                .addChild(infoCommand(), "info")
                .build();
    }

    private Command.Parameterized reloadCommand() {
        return Command.builder()
                .executor(ctx -> {
                    PlatformSender sender = plugin.platform().wrapSender(ctx.cause().first(Player.class).orElse(null));
                    handleCommand(sender, new String[]{"reload"});
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized startCommand() {
        Parameter.Value<String> gameParam = Parameter.string().key("game").build();

        return Command.builder()
                .addParameter(gameParam)
                .executor(ctx -> {
                    PlatformSender sender = plugin.platform().wrapSender(ctx.cause().first(Player.class).orElse(null));
                    String game = ctx.requireOne(gameParam);
                    handleCommand(sender, new String[]{"start", game});
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized stopCommand() {
        return Command.builder()
                .executor(ctx -> {
                    PlatformSender sender = plugin.platform().wrapSender(ctx.cause().first(Player.class).orElse(null));
                    handleCommand(sender, new String[]{"stop"});
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized listCommand() {
        return Command.builder()
                .executor(ctx -> {
                    PlatformSender sender = plugin.platform().wrapSender(ctx.cause().first(Player.class).orElse(null));
                    handleCommand(sender, new String[]{"list"});
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized toggleCommand() {
        return Command.builder()
                .executor(ctx -> {
                    PlatformSender sender = plugin.platform().wrapSender(ctx.cause().first(Player.class).orElse(null));
                    handleCommand(sender, new String[]{"toggle"});
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized infoCommand() {
        return Command.builder()
                .executor(ctx -> {
                    PlatformSender sender = plugin.platform().wrapSender(ctx.cause().first(Player.class).orElse(null));
                    handleCommand(sender, new String[]{"info"});
                    return CommandResult.success();
                })
                .build();
    }
}
