package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.command.SpigotChatGamesCommand;
import dev.rarehyperion.chatgames.listener.SpigotChatListener;
import dev.rarehyperion.chatgames.platform.Platform;
import dev.rarehyperion.chatgames.platform.PlatformPluginMeta;
import dev.rarehyperion.chatgames.platform.PlatformSender;
import dev.rarehyperion.chatgames.platform.PlatformTask;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class SpigotPlatform implements Platform {

    private final JavaPlugin plugin;

    public SpigotPlatform(final JavaPlugin plugin) {
        this.plugin = plugin;
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
    public void sendMessage(final UUID recipientUuid, final Component component) {
        final String legacy = MessageUtil.serialize(component);
        final Player player = this.plugin.getServer().getPlayer(recipientUuid);
        if(player == null) throw new IllegalStateException("Unable to find player matching uuid: " + recipientUuid);
        player.sendMessage(legacy);
    }

    @Override
    public void broadcast(final Component component) {
        final String legacy = MessageUtil.serialize(component);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(legacy));
    }

    @Override
    public void sendConsole(final Component component) {}

    @Override
    public void dispatchCommand(String command) {}

    @Override
    public void registerCommands(final ChatGamesCore core) {
        final SpigotChatGamesCommand command = new SpigotChatGamesCommand(core);
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
                .toList();
    }

    @Override
    public PlatformTask runTask(final Runnable task) {
        return new SpigotPlatformTask(Bukkit.getScheduler().runTask(this.plugin, task));
    }

    @Override
    public PlatformTask runTaskAsync(final Runnable task) {
        return new SpigotPlatformTask(Bukkit.getScheduler().runTaskAsynchronously(this.plugin, task));
    }

    @Override
    public PlatformTask runTaskLater(final Runnable task, final long ticks) {
        return new SpigotPlatformTask(Bukkit.getScheduler().runTaskLater(this.plugin, task, ticks));
    }

    @Override
    public PlatformTask runTaskTimer(Runnable task, long initialDelay, long periodTicks) {
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
        if(sender instanceof CommandSender commandSender) {
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
    public String playerName(final UUID uuid) {
        return Objects.requireNonNull(Bukkit.getPlayer(uuid)).getName();
    }

    @Override
    public Logger getLogger() {
        return this.plugin.getLogger();
    }

}
