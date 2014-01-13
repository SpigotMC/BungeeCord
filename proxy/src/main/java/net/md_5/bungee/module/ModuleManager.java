package net.md_5.bungee.module;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.PluginDescription;
import org.yaml.snakeyaml.Yaml;

public class ModuleManager
{

    private final Map<String, ModuleSource> knownSources = new HashMap<>();

    public ModuleManager()
    {
        knownSources.put( "jenkins", new JenkinsModuleSource() );
    }

    public void load(ProxyServer proxy) throws Exception
    {
        ModuleVersion bungeeVersion = ModuleVersion.parse( "git:BungeeCord-Proxy:1.7-SNAPSHOT:\"93cf50b\":792" );

        File moduleDirectory = new File( "modules" );
        moduleDirectory.mkdir();

        Map<String, ModuleSource> modules = new HashMap<>();

        // TODO: Use filename filter here and in PluginManager
        Yaml yaml = new Yaml();
        for ( File file : moduleDirectory.listFiles() )
        {
            if ( file.isFile() && file.getName().endsWith( ".jar" ) )
            {
                String moduleName = file.getName().substring( 0, file.getName().length() - 4 ); // 4 = .jar.length()
                ModuleSource source = modules.get( moduleName );
                if ( source == null )
                {
                    System.out.println( "No source for module in file: " + file );
                    continue;
                }

                try ( JarFile jar = new JarFile( file ) )
                {
                    JarEntry pdf = jar.getJarEntry( "plugin.yml" );
                    Preconditions.checkNotNull( pdf, "Plugin must have a plugin.yml" );

                    try ( InputStream in = jar.getInputStream( pdf ) )
                    {
                        PluginDescription desc = yaml.loadAs( in, PluginDescription.class );
                        ModuleVersion moduleVersion = ModuleVersion.parse( desc.getVersion() );
                        if ( !moduleVersion.equals( bungeeVersion ) )
                        {
                            System.out.println( "Attempting to update plugin from " + moduleVersion + " to " + bungeeVersion );
                            source.retrieve( new ModuleSpec( moduleName, file ), bungeeVersion );
                        }
                    }
                } catch ( Exception ex )
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not check module from file " + file, ex );
                }
            }
        }
    }
}
