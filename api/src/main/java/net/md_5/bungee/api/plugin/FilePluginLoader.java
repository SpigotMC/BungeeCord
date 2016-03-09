package net.md_5.bungee.api.plugin;

import java.io.File;
import java.util.Collection;

/**
 * PluginLoader that loads from a File.
 */
interface FilePluginLoader extends PluginLoader
{

    /**
     * Attempt to load a plugin / plugins from the given file.
     *
     * @return null if this loader does not support the given file or a list of plugins that were loaded, otherwise a collection of available plugins.
     */
    Collection<AvailablePluginWrapper> listPlugins(PluginManager manager, File file);
}
