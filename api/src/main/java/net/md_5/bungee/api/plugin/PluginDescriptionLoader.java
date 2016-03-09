package net.md_5.bungee.api.plugin;

import java.io.File;

/**
 * Loader that loads plugin metadata (plugin.yml etc) from a directory or jar file.
 */
interface PluginDescriptionLoader extends PluginLoader
{

    /**
     * Try to load a plugin description from a jar file.
     *
     * @return the plugin description or null if none could be found.
     */
    PluginDescription loadFromJar(PluginManager manager, File file);

    /**
     * Try to load a plugin description from a directory.
     *
     * @return the plugin description or null if none could be found.
     */
    PluginDescription loadFromDirectory(PluginManager manager, File file);
}
