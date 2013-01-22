package net.md_5.bungee.api.plugin;

import lombok.Data;

/**
 * POJO representing the plugin.yml file.
 */
@Data
public class PluginDescription
{

    /**
     * Friendly name of the plugin.
     */
    private final String name;
    /**
     * Plugin main class. Needs to extend {@link Plugin}.
     */
    private final String main;
    /**
     * Plugin version.
     */
    private final String version;
    /**
     * Plugin author.
     */
    private final String author;
}
