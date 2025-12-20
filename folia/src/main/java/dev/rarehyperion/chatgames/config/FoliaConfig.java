package dev.rarehyperion.chatgames.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Set;

public final class FoliaConfig implements Config {

    private final FileConfiguration delegate;

    public FoliaConfig(final FileConfiguration delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getString(final String path, final String def) {
        return this.delegate.getString(path, def);
    }

    @Override
    public int getInt(final String path, final int def) {
        return this.delegate.getInt(path, def);
    }

    @Override
    public boolean getBoolean(final String path, final boolean def) {
        return this.delegate.getBoolean(path, def);
    }

    @Override
    public List<String> getStringList(final String path) {
        return this.delegate.getStringList(path);
    }

    @Override
    public List<?> getList(final String path) {
        return this.delegate.getList(path);
    }

    @Override
    public boolean contains(final String path) {
        return this.delegate.contains(path);
    }

    @Override
    public Set<String> getKeys(final boolean deep) {
        return this.delegate.getKeys(deep);
    }

    @Override
    public Config getConfigurationSection(final String path) {
        final ConfigurationSection section = this.delegate.getConfigurationSection(path);
        if(section == null) return null;
        return new FoliaConfigSection(section);
    }

}
