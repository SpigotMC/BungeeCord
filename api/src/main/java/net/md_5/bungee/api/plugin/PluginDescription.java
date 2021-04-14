package net.md_5.bungee.api.plugin;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO representing the plugin.yml file.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginDescription
{

    /**
     * Friendly name of the plugin.
     */
    private String name;
    /**
     * Plugin main class. Needs to extend {@link Plugin}.
     */
    private String main;
    /**
     * Plugin version.
     */
    private String version;
    /**
     * Plugin author.
     */
    private String author;
    /**
     * Plugin hard dependencies.
     */
    private Set<String> depends = new HashSet<>();
    /**
     * Plugin soft dependencies.
     */
    private Set<String> softDepends = new HashSet<>();
    /**
     * File we were loaded from.
     */
    private File file = null;
    /**
     * Optional description.
     */
    private String description = null;
    /**
     * Optional libraries.
     */
    private List<String> libraries = new LinkedList<>();
}
