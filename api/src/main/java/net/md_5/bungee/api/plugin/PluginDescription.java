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
    /**
     * Optional: libraries to be excluded from download. A clear use example is having
     * 2 versions of the same library. This can be achieved by having a library
     * which packs a library the plugin already packs via {@link #getLibraries()}
     * and suddenly, BungeeCord ends up with 2 different versions of the same library,
     * when there can be 1! Or another hilarious case: having a download of a library,
     * already packed in the BungeeCord jar!
     * <p>Syntax is the same as in {@link #getLibraries()}
     */
    private List<String> excludedTransitiveLibraries = new LinkedList<>();
}
