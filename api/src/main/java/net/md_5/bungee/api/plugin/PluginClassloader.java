package net.md_5.bungee.api.plugin;

import java.net.URL;
import java.net.URLClassLoader;
import lombok.ToString;

/**
 * @deprecated Do not use in plugins! Subject to removal or change, use {@linkplain net.md_5.bungee.api.plugin.ProxyPluginClassLoader} instead
 */
@Deprecated
@ToString(of = "desc")
abstract class PluginClassloader extends URLClassLoader implements ProxyPluginClassLoader
{
    /**
     * @deprecated Do not use in plugins! Subject to removal or change, use {@linkplain net.md_5.bungee.api.plugin.ProxyPluginClassLoader} instead
     */
    @Deprecated
    private final PluginDescription desc;

    static
    {
        ClassLoader.registerAsParallelCapable();
    }

    PluginClassloader(URL[] urls, PluginDescription desc)
    {
        super( urls );
        this.desc = desc;
    }

    @Override
    public void addURL(URL url)
    {
        super.addURL( url );
    }

    /**
     * @param plugin plugin
     * @deprecated Do not use in plugins! Subject to removal or change.
     */
    @Deprecated
    abstract void init(Plugin plugin);
}
