package net.md_5.bungee.api.plugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class PluginClassloader extends URLClassLoader
{

    private static final Set<PluginClassloader> allLoaders = new CopyOnWriteArraySet<>();

    static
    {
        ClassLoader.registerAsParallelCapable();
    }

    public PluginClassloader(URL[] urls)
    {
        super( urls );
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
}
