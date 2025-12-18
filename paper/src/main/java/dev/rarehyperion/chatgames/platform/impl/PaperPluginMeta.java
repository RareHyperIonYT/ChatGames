package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.platform.PlatformPluginMeta;
import io.papermc.paper.plugin.configuration.PluginMeta;

import java.util.List;

public class PaperPluginMeta implements PlatformPluginMeta {

    private final PluginMeta meta;

    public PaperPluginMeta(final PluginMeta meta) {
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
