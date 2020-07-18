package net.md_5.bungee.api.plugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @NotNull
    private String name;
    /**
     * Plugin main class. Needs to extend {@link Plugin}.
     */
    @NotNull
    private String main;
    /**
     * Plugin version.
     */
    @Nullable
    private String version;
    /**
     * Plugin author.
     */
    @Nullable
    private String author;
    /**
     * Plugin hard dependencies.
     */
    @NotNull
    private Set<String> depends = new HashSet<>();
    /**
     * Plugin soft dependencies.
     */
    @NotNull
    private Set<String> softDepends = new HashSet<>();
    /**
     * File we were loaded from.
     */
    @NotNull
    private File file = null;
    /**
     * Optional description.
     */
    @Nullable
    private String description = null;
}
