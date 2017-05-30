package net.md_5.bungee.api.plugin;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.scheduler.GroupedThreadFactory;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 * Represents any Plugin that may be loaded at runtime to enhance existing
 * functionality.
 */
@RequiredArgsConstructor
public class Plugin
{

    @Getter
    private PluginDescription description;
    @Getter
    private ProxyServer proxy;
    @Getter
    private File file;
    @Getter
    private Logger logger;

    private final String configFile;
    @Setter
    private Configuration config;

    public Plugin()
    {
        this( "config.yml" ); // Use config.yml as configuration file by default
    }

    /**
     * Called when the plugin has just been loaded. Most of the proxy will not
     * be initialized, so only use it for registering
     * {@link ConfigurationAdapter}'s and other predefined behavior.
     */
    public void onLoad()
    {
    }

    /**
     * Called when this plugin is enabled.
     */
    public void onEnable()
    {
    }

    /**
     * Called when this plugin is disabled.
     */
    public void onDisable()
    {
    }

    /**
     * Gets the data folder where this plugin may store arbitrary data. It will
     * be a child of {@link ProxyServer#getPluginsFolder()}.
     *
     * @return the data folder of this plugin
     */
    public final File getDataFolder()
    {
        return new File( getProxy().getPluginsFolder(), getDescription().getName() );
    }

    /**
     * Get a resource from within this plugins jar or container. Care must be
     * taken to close the returned stream.
     *
     * @param name the full path name of this resource
     * @return the stream for getting this resource, or null if it does not
     * exist
     */
    public final InputStream getResourceAsStream(String name)
    {
        return getClass().getClassLoader().getResourceAsStream( name );
    }

    /**
     * Called by the loader to initialize the fields in this plugin.
     *
     * @param proxy current proxy instance
     * @param description the description that describes this plugin
     */
    final void init(ProxyServer proxy, PluginDescription description)
    {
        this.proxy = proxy;
        this.description = description;
        this.file = description.getFile();
        this.logger = new PluginLogger( this );
    }

    /**
     * Gets the currently loaded configuration and reloads it if it wasn't
     * loaded yet.
     * @return The currently loaded configuration.
     */
    public Configuration getConfig()
    {
        if ( config == null )
        {
            reloadConfig();
        }

        return config;
    }

    /**
     * Reload the configuration from disk or copies the default configuration
     * from the plugin.
     * @return Whether the operation was successful.
     */
    public boolean reloadConfig()
    {
        // Check if the plugin folder exists or create it if not
        File dataFolder = getDataFolder();
        if ( !dataFolder.exists() )
        {
            dataFolder.mkdirs();
        }

        ConfigurationProvider provider = ConfigurationProvider.getProvider( YamlConfiguration.class );

        // Load the configuration defaults first
        Configuration defaults = null;
        try ( InputStream is = getResourceAsStream( configFile ) )
        {
            if ( is != null )
            {
                defaults = provider.load( is );
            }
        } catch ( Exception e )
        {
            getLogger().log( Level.WARNING, "Failed to load default configuration", e );
        }

        Configuration config = null;

        File file = new File( dataFolder, configFile );
        if ( file.exists() )
        {
            // Load the configuration
            try
            {
                config = provider.load( file, defaults );
            } catch ( Exception e )
            {
                getLogger().log( Level.SEVERE, "Failed to load configuration", e );
            }
        } else
        {
            config = new Configuration( defaults );

            // Copy the default configuration
            try ( InputStream is = getResourceAsStream( configFile ) )
            {
                if (is != null)
                {
                    Files.copy(is, file.toPath());
                }
            } catch ( IOException e )
            {
                getLogger().log( Level.WARNING, "Failed to copy default configuration", e );
            }
        }

        // Check if configuration loading failed
        if ( config == null )
        {
            this.config = new Configuration( defaults );
            return false;
        }

        // Replace the currently loaded configuration
        this.config = config;
        return true;
    }

    /**
     * Saves the currently loaded configuration to the disk.
     * @return Whether the operation was successful.
     */
    public boolean saveConfig() {
        // Check if the configuration is loaded
        if ( config != null )
        {
            File file = new File( getDataFolder(), configFile );
            try
            { // Save the configuration
                ConfigurationProvider.getProvider( YamlConfiguration.class ).save( config, file );
            } catch ( IOException e )
            {
                getLogger().log( Level.SEVERE, "Failed to save configuration", e );
                return false;
            }
        }

        return true;
    }

    //
    private ExecutorService service;

    @Deprecated
    public ExecutorService getExecutorService()
    {
        if ( service == null )
        {
            String name = ( getDescription() == null ) ? "unknown" : getDescription().getName();
            service = Executors.newCachedThreadPool( new ThreadFactoryBuilder().setNameFormat( name + " Pool Thread #%1$d" )
                    .setThreadFactory( new GroupedThreadFactory( this, name ) ).build() );
        }
        return service;
    }
    //
}
