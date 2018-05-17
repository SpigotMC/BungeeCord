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
    private File configurationFile;
    private Configuration configuration;

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
     * (Re)load the {@link Configuration} of this plugin from the disk. The config file is config.yml
     */
    public void loadConfig()
    {
        try
        {
            if ( !getDataFolder().exists() )
            {
                getDataFolder().mkdir();
            }

            if ( !configurationFile.exists() )
            {
                try ( InputStream in = getResourceAsStream( "config.yml" ) )
                {
                    Files.copy( in, configurationFile.toPath() );
                }
            }
            configuration = ConfigurationProvider.getProvider( YamlConfiguration.class ).load( configurationFile );
        } catch ( IOException ex )
        {
            logger.log( Level.SEVERE, "Could not load the configuration", ex );
        }
    }

    /**
     * Save the {@link Configuration} of this plugin to the disk.
     */
    public void saveConfig() {
        if ( configuration != null )
        {
            try
            {
                ConfigurationProvider.getProvider( YamlConfiguration.class ).save( configuration, configurationFile );
            } catch (IOException ex)
            {
                logger.log( Level.SEVERE, "Could not save the configuration", ex );
            }
        }
    }

    /**
     * Get the {@link Configuration} of this plugin.
     *
     * @return Plugin configuration
     */
    public Configuration getConfig()
    {
        if ( configuration == null )
        {
            loadConfig();
        }
        return configuration;
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
        this.configurationFile = new File( getDataFolder(), "config.yml" );
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
