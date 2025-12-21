package dev.rarehyperion.chatgames.platform.impl;

import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.command.FoliaChatGamesCommand;
import dev.rarehyperion.chatgames.config.Config;
import dev.rarehyperion.chatgames.config.FoliaConfig;
import dev.rarehyperion.chatgames.listener.FoliaChatListener;
import dev.rarehyperion.chatgames.platform.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"UnstableApiUsage"})
public class FoliaPlatform implements Platform {

    private final JavaPlugin plugin;
    private final PlatformLogger logger;

    public FoliaPlatform(final JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = new FoliaPlatformLogger(this.plugin.getLogger());
    }

    @Override
    public String name() {
        return "FOLIA";
    }

    @Override
    public PlatformPluginMeta pluginMeta() {
        return new FoliaPluginMeta(this.plugin.getPluginMeta());
    }

    @Override
    public void broadcast(final Component component) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(component));
    }

    @Override
    public void dispatchCommand(final String command) {
        this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), command);
    }

    @Override
    public void registerCommands(final ChatGamesCore core) {
        // Papers API genuinely drives me insane... almost as much as Fabric API which says a lot.

        final FoliaChatGamesCommand command = new FoliaChatGamesCommand(core);
        final LiteralCommandNode<CommandSourceStack> mainNode = command.build();

        // Aliases don't work as expected, so Folia won't have command aliases for now unfortunately.
//      final LiteralCommandNode<CommandSourceStack> aliasCg = Commands.literal("cg").redirect(mainNode).build();
//      final LiteralCommandNode<CommandSourceStack> aliasChatgame = Commands.literal("chatgame").redirect(mainNode).build();

        this.plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(mainNode);
//          commands.register(aliasCg);
//          commands.register(aliasChatgame);
        });
    }

    @Override
    public void registerListeners(final ChatGamesCore core) {
        this.plugin.getServer().getPluginManager().registerEvents(new FoliaChatListener(core.gameManager()), this.plugin);
    }

    @Override
    public Collection<UUID> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers()
                .stream()
                .map(Entity::getUniqueId)
                .collect(Collectors.toList());
    }

    @Override
    public PlatformTask runTask(final Runnable task) {
        final ScheduledTask scheduledTask = Bukkit.getGlobalRegionScheduler().run(this.plugin, t -> task.run());
        return new FoliaPlatformTask(scheduledTask);
    }

    @Override
    public PlatformTask runTaskLater(final Runnable task, final long ticks) {
        final ScheduledTask scheduledTask = Bukkit.getGlobalRegionScheduler().runDelayed(this.plugin, t -> task.run(), ticks);
        return new FoliaPlatformTask(scheduledTask);
    }

    @Override
    public PlatformTask runTaskTimer(final Runnable task, final long initialDelay, final long periodTicks) {
        final ScheduledTask scheduledTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.plugin, t -> task.run(), initialDelay, periodTicks);
        return new FoliaPlatformTask(scheduledTask);
    }

    @Override
    public void saveDefaultConfig() {
        this.plugin.saveDefaultConfig();
    }

    @Override
    public void reloadConfig() {
        this.plugin.reloadConfig();
    }

    @Override
    public PlatformSender wrapSender(final Object sender) {
        if(sender instanceof CommandSender) {
            final CommandSender commandSender = (CommandSender) sender;
            return new FoliaPlatformSender(commandSender);
        }

        throw new IllegalArgumentException("Unsupported: " + sender);
    }

    @Override
    public <T> T getConfigValue(final String path, final Class<T> type, final T defaultValue) {
        if (!this.plugin.getConfig().contains(path)) {
            return defaultValue;
        }

        final Object value = this.plugin.getConfig().get(path);

        if (value == null) {
            return defaultValue;
        }

        if (!type.isInstance(value)) {
            this.plugin.getLogger().warning("Config value at '" + path + "' is not of type " + type.getSimpleName());
            return defaultValue;
        }

        return type.cast(value);
    }

    @Override
    public void setConfigValue(final String path, final Object value) {
        this.plugin.getConfig().set(path, value);
    }

    @Override
    public Config loadConfig(final File file) {
        return new FoliaConfig(YamlConfiguration.loadConfiguration(file));
    }

    @Override
    public void saveConfig() {
        this.plugin.saveConfig();
    }

    @Override
    public File getDataFolder() {
        return this.plugin.getDataFolder();
    }

    @Override
    public InputStream getResource(final String resourcePath) {
        return this.plugin.getResource(resourcePath);
    }

    @Override
    public PlatformLogger getLogger() {
        return this.logger;
    }

}
