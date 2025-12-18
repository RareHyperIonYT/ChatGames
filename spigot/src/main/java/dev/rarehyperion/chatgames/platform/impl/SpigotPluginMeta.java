package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.platform.PlatformPluginMeta;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.List;

public class SpigotPluginMeta implements PlatformPluginMeta {

    private final PluginDescriptionFile meta;

    public SpigotPluginMeta(final PluginDescriptionFile meta) {
        this.meta = meta;
    }

    @Override
    public String getName() {
        return this.meta.getName();
    }

    @Override
    public String getVersion() {
        return this.meta.getVersion();
    }

    @Override
    public List<String> getAuthors() {
        return this.meta.getAuthors();
    }

}
