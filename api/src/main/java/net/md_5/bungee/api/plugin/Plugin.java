package net.md_5.bungee.api.plugin;

import java.io.File;
import java.io.InputStream;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ConfigurationAdapter;

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
    private File jarfile;
    
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
     * @param description the description that describes this plugin
     * @param jarfile this plugins jar or container
     */
    final void init(ProxyServer proxy, PluginDescription description, File jarfile)
    {
        this.proxy = proxy;
        this.description = description;
        this.jarfile = jarfile;
    }
}
