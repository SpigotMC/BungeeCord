package net.md_5.bungee.api.plugin;

import lombok.RequiredArgsConstructor;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;

/**
 * FilePluginLoader that loads jar files.
 */
class JarPluginLoader implements FilePluginLoader
{

    @Override
    public Collection<AvailablePluginWrapper> listPlugins(PluginManager manager, final File file)
    {
        // only accept jar files
        if ( !file.isFile() || !file.getName().endsWith( ".jar" ) )
        {
            return null;
        }

        final PluginDescription desc = loadDescription( manager, file );

        if ( desc == null )
        {
            return null;
        }

        desc.setFile( file );

        // found a plugin, return that
        return Collections.<AvailablePluginWrapper>singleton( new AvailablePluginWrapper( desc )
        {
            @Override
            public Plugin loadPlugin(PluginManager manager) throws Exception
            {
                URLClassLoader loader = new PluginClassloader( new URL[]{ file.toURI().toURL() } );
                Class<?> main = loader.loadClass( desc.getMain() );
                return (Plugin) main.getDeclaredConstructor().newInstance();
            }
        } );
    }

    private PluginDescription loadDescription(PluginManager manager, File file)
    {
        for ( PluginDescriptionLoader loader : manager.getPluginLoadersByType( PluginDescriptionLoader.class ) )
        {
            PluginDescription desc = loader.loadFromJar( manager, file );
            if ( desc != null )
            {
                return desc;
            }
        }
        return null;
    }
}
