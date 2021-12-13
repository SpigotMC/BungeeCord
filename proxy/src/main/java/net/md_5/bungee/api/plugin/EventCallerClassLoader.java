package net.md_5.bungee.api.plugin;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class loader which can delegate to all {@linkplain net.md_5.bungee.api.plugin.PluginClassloader PluginClassloader}
 * so classes loaded by it can access all plugin classes.<br>
 * <br>
 * The class is in this package to be able to easily access PluginClassloader.allLoaders
 */
public final class EventCallerClassLoader extends ClassLoader
{

    private static final String EVENT_CALLER_CLASS = "net.md_5.bungee.pluginutil.EventCaller";
    private static final String EVENT_CALLER_CLASS_PATH = EVENT_CALLER_CLASS.replace( '.', '/' ).concat( ".class" );

    static
    {
        ClassLoader.registerAsParallelCapable();
    }

    private static MethodHandles.Lookup lookup;

    public static synchronized MethodHandles.Lookup getEventCallerLookup()
    {
        if ( lookup == null )
        {
            try
            {
                lookup = (MethodHandles.Lookup) new EventCallerClassLoader().loadEventCallerClass().getDeclaredField( "lookup" ).get( null );
            } catch ( ReflectiveOperationException ex )
            {
                throw new RuntimeException( ex );
            }
        }
        return lookup;
    }

    public static void registerClassloader(ClassLoader classLoader)
    {
        if ( classLoader instanceof PluginClassloader && pluginClassLoaders.contains( classLoader ) )
        {
            return;
        }
        additionalClassLoaders.add( classLoader );
    }

    private static final Set<PluginClassloader> pluginClassLoaders = PluginClassloader.allLoaders;
    private static final Set<ClassLoader> additionalClassLoaders = Collections.newSetFromMap( new ConcurrentHashMap<>() );
    private final ClassLoader appClassLoader;

    private EventCallerClassLoader()
    {
        this.appClassLoader = getClass().getClassLoader();
    }

    private Class<?> eventCallerClass;

    private Class<?> loadEventCallerClass()
    {
        if ( eventCallerClass == null )
        {
            try
            {
                return eventCallerClass = findClass( EVENT_CALLER_CLASS );
            } catch ( ClassNotFoundException ex )
            {
                throw new RuntimeException( ex );
            }
        }
        return eventCallerClass;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        if ( EVENT_CALLER_CLASS.equals( name ) )
        {
            synchronized ( getClassLoadingLock( name ) )
            {
                Class<?> c = loadEventCallerClass();
                if ( resolve )
                {
                    resolveClass( c );
                }
                return c;
            }
        }
        return super.loadClass( name, resolve );
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        if ( EVENT_CALLER_CLASS.equals( name ) )
        {
            if ( eventCallerClass != null )
            {
                return eventCallerClass;
            }
            try
            {
                byte[] bytes = ByteStreams.toByteArray( appClassLoader.getResource( EVENT_CALLER_CLASS_PATH ).openStream() );
                // use define class and don't delegate to app classloader so we are the classloader
                return defineClass( EVENT_CALLER_CLASS, bytes, 0, bytes.length, getClass().getProtectionDomain() );
            } catch ( NullPointerException | IOException ex )
            {
                throw new ClassNotFoundException( EVENT_CALLER_CLASS, ex );
            }
        }
        for ( ClassLoader classLoader : pluginClassLoaders )
        {
            try
            {
                return classLoader.loadClass( name );
            } catch ( ClassNotFoundException ignored )
            {
            }
        }
        for ( ClassLoader classLoader : additionalClassLoaders )
        {
            try
            {
                return classLoader.loadClass( name );
            } catch ( ClassNotFoundException ignored )
            {
            }
        }
        return appClassLoader.loadClass( name );
    }
}
