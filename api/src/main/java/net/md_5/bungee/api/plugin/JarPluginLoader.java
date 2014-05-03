package net.md_5.bungee.api.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;

/**
 * Abstract FilePluginLoader that loads jar files. Subclasses implement description detection behaviour (bungee.yml, annotations etc).
 */
abstract class JarPluginLoader implements FilePluginLoader
{

    @Override
    public Collection<AvailablePluginWrapper> listPlugins(PluginManager manager, final File file)
    {
        // only accept jar files
        if ( !file.isFile() || !file.getName().endsWith( ".jar" ) )
        {
            return null;
        }

        final PluginDescription desc = loadDescription( file );

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

    /**
     * Try to load a plugin description from a jar file.
     *
     * @return the plugin description or null if none could be found.
     */
    protected abstract PluginDescription loadDescription(File file);
}
