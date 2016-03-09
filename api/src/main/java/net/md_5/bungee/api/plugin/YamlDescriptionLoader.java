package net.md_5.bungee.api.plugin;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ProxyServer;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * PluginDescriptionLoader that uses plugin.yml or bungee.yml for plugin descriptions.
 */
class YamlDescriptionLoader implements PluginDescriptionLoader
{

    private final Yaml yaml = new Yaml();

    @Override
    public PluginDescription loadFromJar(PluginManager manager, File file)
    {
        try ( JarFile jar = new JarFile( file ) )
        {
            JarEntry pdf = jar.getJarEntry( "bungee.yml" );
            if ( pdf == null )
            {
                pdf = jar.getJarEntry( "plugin.yml" );
            }

            // TODO: If we ever have multiple PluginDescriptionLoaders we need to move this to another class at the end of the loader list
            Preconditions.checkNotNull( pdf, "Plugin must have a plugin.yml or bungee.yml, " + file.getName() + "has neither" );

            try ( InputStream in = jar.getInputStream( pdf ) )
            {
                return loadDescription( in );
            }
        } catch ( Exception ex )
        {
            ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load plugin from file " + file, ex );
            return null;
        }
    }

    @Override
    public PluginDescription loadFromDirectory(PluginManager manager, File file)
    {
        try
        {
            File pdf = new File( file, "bungee.yml" );
            if ( !pdf.exists() )
            {
                pdf = new File( file, "plugin.yml" );
                if ( !pdf.exists() )
                {
                    // fail silently
                    return null;
                }
            }

            try ( InputStream in = new FileInputStream( pdf ) )
            {
                return loadDescription( in );
            }
        } catch ( Exception ex )
        {
            ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load plugin from file " + file, ex );
            return null;
        }
    }

    private PluginDescription loadDescription(InputStream in)
    {
        return yaml.loadAs( in, PluginDescription.class );
    }
}
