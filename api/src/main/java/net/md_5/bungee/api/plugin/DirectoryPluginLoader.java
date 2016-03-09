package net.md_5.bungee.api.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * FilePluginLoader that (recursively) loads jars in a plugin folder.
 */
class DirectoryPluginLoader implements FilePluginLoader
{

    @Override
    public Collection<AvailablePluginWrapper> listPlugins(PluginManager manager, File source)
    {
        // only directories are supported
        if ( !source.isDirectory() )
        {
            return null;
        }

        Collection<AvailablePluginWrapper> plugins = new ArrayList<>();
        for ( File child : source.listFiles() )
        {
            // note that this usually returns the DirectoryPluginLoader as well, plugins in subdirectories will be loaded
            for ( FilePluginLoader childLoader : manager.getPluginLoadersByType( FilePluginLoader.class ) )
            {
                Collection<AvailablePluginWrapper> loaded = childLoader.listPlugins( manager, child );
                if ( loaded != null )
                {
                    plugins.addAll( loaded );
                    break;
                }
            }
        }
        return plugins;
    }
}
