package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.platform.PlatformPluginMeta;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.util.List;

public class SpongePluginMeta implements PlatformPluginMeta {

    private final PluginMetadata meta;

    public SpongePluginMeta(final PluginMetadata meta) {
        this.meta = meta;
    }

    @Override
    public String getName() {
        return this.meta.name().orElse("ChatGames-DEBUG");
    }

    @Override
    public String getVersion() {
        return this.meta.version().toString();
    }

    @Override
    public List<String> getAuthors() {
        return List.of("RareHyperIon");
    }

}
