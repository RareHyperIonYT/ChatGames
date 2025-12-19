package dev.rarehyperion.chatgames;

import com.google.inject.Inject;
import dev.rarehyperion.chatgames.command.SpongeChatGamesCommand;
import dev.rarehyperion.chatgames.platform.impl.SpongePlatform;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.nio.file.Path;

@Plugin("chatgames")
public class ChatGamesSponge {

    @Inject private PluginContainer container;
    @Inject private Logger logger;

    @Inject @DefaultConfig(sharedRoot = false)
    private Path configPath;

    @Inject @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;

    @Inject @ConfigDir(sharedRoot = false) private Path privateConfigDir;

    private ChatGamesCore core;

    @Listener
    public void onConstruct(final ConstructPluginEvent event) {
        this.core = new ChatGamesCore(new SpongePlatform(this.container, this.logger, this.configPath, this.configLoader, this.privateConfigDir));
    }

    @Listener
    public void onServerLoad(final StartingEngineEvent<Server> event) {
        this.core.load();
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.container, new SpongeChatGamesCommand(this.core).build(), "chatgames", "chatgame", "cg");
    }

    @Listener
    public void onServerStart(final StartedEngineEvent<Server> event) {
        this.core.enable();
    }

    @Listener
    public void onServerStop(final StoppingEngineEvent<Server> event) {
        this.core.disable();
    }


}
