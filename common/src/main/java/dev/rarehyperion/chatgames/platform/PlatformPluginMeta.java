package dev.rarehyperion.chatgames.platform;

import java.util.List;

/**
 * Represents metadata about the plugin.
 *
 * <p>
 *     Provides information such as the plugin's name, version, and authors.
 * </p>
 *
 * @author RareHyperIon
 * @since 20/12/2025
 */
public interface PlatformPluginMeta {

    /**
     * Returns the name of the plugin.
     * @return The plugin name.
     */
    String getName();

    /**
     * Returns the version of the plugin.
     * @return The plugin version.
     */
    String getVersion();

    /**
     * Returns the authors of the plugin.
     * @return A list of author names.
     */
    List<String> getAuthors();

}
