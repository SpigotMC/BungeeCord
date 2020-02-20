package net.md_5.bungee.api.plugin;

import com.google.common.base.Preconditions;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import net.md_5.bungee.api.ProxyServer;

final class PluginClassloader extends URLClassLoader
{

    private static final Set<PluginClassloader> allLoaders = new CopyOnWriteArraySet<>();
    //
    private final ProxyServer proxy;
    private final PluginDescription desc;
    //
    private Plugin plugin;

    static
    {
        ClassLoader.registerAsParallelCapable();
    }

    public PluginClassloader(ProxyServer proxy, PluginDescription desc, URL[] urls)
    {
        super( urls );
        this.proxy = proxy;
        this.desc = desc;

        allLoaders.add( this );
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        return loadClass0( name, resolve, true );
    }

    private Class<?> loadClass0(String name, boolean resolve, boolean checkOther) throws ClassNotFoundException
    {
        try
        {
            return super.loadClass( name, resolve );
        } catch ( ClassNotFoundException ex )
        {
        }
        if ( checkOther )
        {
            for ( PluginClassloader loader : allLoaders )
            {
                if ( loader != this )
                {
                    try
                    {
                        return loader.loadClass0( name, resolve, false );
                    } catch ( ClassNotFoundException ex )
                    {
                    }
                }
            }
        }
        throw new ClassNotFoundException( name );
    }

    void init(Plugin plugin)
    {
        Preconditions.checkArgument( plugin != null, "plugin" );
        Preconditions.checkArgument( plugin.getClass().getClassLoader() == this, "Plugin has incorrect ClassLoader" );
        if ( this.plugin != null )
        {
            throw new IllegalArgumentException( "Plugin already initialized!" );
        }

        this.plugin = plugin;
        plugin.init( proxy, desc );
    }
}
