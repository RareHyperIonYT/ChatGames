package dev.rarehyperion.chatgames.config;

import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;

public class SpongeConfig implements Config {

    private final CommentedConfigurationNode root;

    public SpongeConfig(final CommentedConfigurationNode root) {
        this.root = root;
    }

    private CommentedConfigurationNode node(final String path) {
        if (path == null || path.trim().isEmpty()) return this.root;
        final String[] parts = path.split("\\.");
        return this.root.node((Object[]) parts);
    }

    @Override
    public String getString(final String path, final String def) {
        return this.node(path).getString(def);
    }

    @Override
    public int getInt(final String path, final int def) {
        return this.node(path).getInt(def);
    }

    @Override
    public boolean getBoolean(final String path, final boolean def) {
        return this.node(path).getBoolean(def);
    }

    @Override
    public List<String> getStringList(final String path) {
        try {
            return this.node(path).getList(TypeToken.get(String.class), Collections.emptyList());
        } catch (final Exception exception) {
            exception.printStackTrace(System.err);
            return Collections.emptyList();
        }
    }

    @Override
    public List<?> getList(final String path) {
        final Object raw = this.node(path).raw();

        if (raw instanceof List<?>) {
            return (List<?>) raw;
        } else if (raw instanceof Map<?, ?>) {
            return new ArrayList<>(((Map<?, ?>) raw).values());
        } else if (raw == null) {
            return null;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean contains(final String path) {
        final CommentedConfigurationNode node = this.node(path);
        return !node.virtual();
    }

    @Override
    public Set<String> getKeys(final boolean deep) {
        final Set<String> keys = new LinkedHashSet<>();

        ((ConfigurationNode) this.root).childrenMap().forEach((key, child) -> {
            keys.add(key.toString());

            if (deep) {
                Set<String> childKeys = getKeysRecursive(child, key.toString());
                keys.addAll(childKeys);
            }
        });

        return keys;
    }

    private Set<String> getKeysRecursive(final ConfigurationNode node, final String parent) {
        final Set<String> keys = new LinkedHashSet<>();

        node.childrenMap().forEach((key, child) -> {
            String fullKey = parent + "." + key.toString();
            keys.add(fullKey);
            keys.addAll(getKeysRecursive(child, fullKey));
        });

        return keys;
    }

    @Override
    public Config getConfigurationSection(final String path) {
        return new SpongeConfig(this.node(path));
    }

}
