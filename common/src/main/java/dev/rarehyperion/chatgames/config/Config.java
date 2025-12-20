package dev.rarehyperion.chatgames.config;

import java.util.List;
import java.util.Set;

public interface Config {

    String getString(final String path, final String def);
    int getInt(final String path, int def);
    boolean getBoolean(final String path, boolean def);

    List<String> getStringList(final String path);
    List<?> getList(final String path);

    boolean contains(final String path);
    Set<String> getKeys(final boolean deep);

    Config getConfigurationSection(final String path);

}
