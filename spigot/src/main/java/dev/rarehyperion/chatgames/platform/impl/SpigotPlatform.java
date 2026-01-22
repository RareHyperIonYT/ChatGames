package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.command.CommandRegistry;
import dev.rarehyperion.chatgames.command.SpigotChatGamesCommand;
import dev.rarehyperion.chatgames.config.Config;
import dev.rarehyperion.chatgames.config.SpigotConfig;
import dev.rarehyperion.chatgames.events.ChatGameEndEvent;
import dev.rarehyperion.chatgames.events.ChatGameStartEvent;
import dev.rarehyperion.chatgames.events.ChatGameWinEvent;
import dev.rarehyperion.chatgames.game.EndReason;
import dev.rarehyperion.chatgames.game.GameType;
import dev.rarehyperion.chatgames.listener.SpigotChatListener;
import dev.rarehyperion.chatgames.platform.*;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpigotPlatform implements Platform {

    private final JavaPlugin plugin;
    private final PlatformLogger logger;

    public SpigotPlatform(final JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = new SpigotPlatformLogger(this.plugin.getLogger());
    }

    @Override
    public String name() {
        return "SPIGOT";
    }

    @Override
    public PlatformPluginMeta pluginMeta() {
        return new SpigotPluginMeta(this.plugin.getDescription());
    }

    @Override
    public void broadcast(final Component component) {
        final String legacy = MessageUtil.serialize(component);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(legacy));
    }

    @Override
    public void dispatchCommand(String command) {}

    @Override
    public void registerCommands(final ChatGamesCore core) {
        final CommandRegistry registry = new CommandRegistry(core);
        core.setCommandRegistry(registry);
        final SpigotChatGamesCommand command = new SpigotChatGamesCommand(core, registry);
        Objects.requireNonNull(this.plugin.getCommand("chatgames")).setExecutor(command);
        Objects.requireNonNull(this.plugin.getCommand("chatgames")).setTabCompleter(command);
    }

    @Override
    public void registerListeners(final ChatGamesCore core) {
        this.plugin.getServer().getPluginManager().registerEvents(
                new SpigotChatListener(core.gameManager()),
                this.plugin
        );
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
        return new SpigotPlatformTask(Bukkit.getScheduler().runTask(this.plugin, task));
    }

    @Override
    public PlatformTask runTaskLater(final Runnable task, final long ticks) {
        return new SpigotPlatformTask(Bukkit.getScheduler().runTaskLater(this.plugin, task, ticks));
    }

    @Override
    public PlatformTask runTaskTimer(final Runnable task, final long initialDelay, final long periodTicks) {
        return new SpigotPlatformTask(Bukkit.getScheduler().runTaskTimer(this.plugin, task, initialDelay, periodTicks));
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
            return new SpigotPlatformSender(commandSender);
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
        return new SpigotConfig(YamlConfiguration.loadConfiguration(file));
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

    @Override
    public void dispatchStart(final GameType type, final String question, final String answer, final List<String> rewards) {
        Bukkit.getPluginManager().callEvent(new ChatGameStartEvent(type, question, answer, rewards));
    }

    @Override
    public void dispatchWin(final PlatformPlayer pp, final GameType type, final String question, final String answer, final List<String> rewards) {
        final Player player = Bukkit.getPlayer(pp.id());
        Bukkit.getPluginManager().callEvent(new ChatGameWinEvent(player, type, question, answer, rewards));
    }

    @Override
    public void dispatchEnd(GameType type, String question, String answer, List<String> rewards, final EndReason reason) {
        Bukkit.getPluginManager().callEvent(new ChatGameEndEvent(type, question, answer, rewards, reason));
    }

}
