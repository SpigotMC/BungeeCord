package net.md_5.bungee.api.plugin;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.AbstractMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.util.LookupFetcherClassCreator;

final class BungeePluginClassLoader extends PluginClassloader
{

    private static final Set<BungeePluginClassLoader> allLoaders = new CopyOnWriteArraySet<>();
    //
    private final ProxyServer proxy;
    @Getter
    private final PluginDescription desc;
    private final JarFile jar;
    private final Manifest manifest;
    private final URL url;
    private final ClassLoader libraryLoader;
    //
    private Plugin plugin;
    @Getter
    private MethodHandles.Lookup lookup;

    static
    {
        ClassLoader.registerAsParallelCapable();
    }

    BungeePluginClassLoader(ProxyServer proxy, PluginDescription description, File file, ClassLoader libraryLoader) throws IOException
    {
        super( new URL[]
        {
            file.toURI().toURL()
        }, description );
        this.proxy = proxy;
        this.desc = description;
        this.jar = new JarFile( file );
        this.manifest = jar.getManifest();
        this.url = file.toURI().toURL();
        this.libraryLoader = libraryLoader;

        allLoaders.add( this );
        createLookup();
    }

    @SuppressWarnings("unchecked")
    private void createLookup()
    {
        AbstractMap.SimpleEntry<String, byte[]> entry = LookupFetcherClassCreator.create();
        try
        {
            Class<?> definedCLass = defineClass( entry.getKey(), entry.getValue(), 0, entry.getValue().length );
            lookup = ( (Supplier<MethodHandles.Lookup>) definedCLass.getConstructor().newInstance() ).get();
        } catch ( ReflectiveOperationException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        return loadClass0( name, resolve, true, true );
    }

    private Class<?> loadClass0(String name, boolean resolve, boolean checkOther, boolean checkLibraries) throws ClassNotFoundException
    {
        try
        {
            return super.loadClass( name, resolve );
        } catch ( ClassNotFoundException ignored )
        {
        }

        if ( checkLibraries && libraryLoader != null )
        {
            try
            {
                return libraryLoader.loadClass( name );
            } catch ( ClassNotFoundException ignored )
            {
            }
        }

        if ( checkOther )
        {
            for ( BungeePluginClassLoader loader : allLoaders )
            {
                if ( loader != this )
                {
                    try
                    {
                        return loader.loadClass0( name, resolve, false, proxy.getPluginManager().isTransitiveDepend( desc, loader.desc ) );
                    } catch ( ClassNotFoundException ignored )
                    {
                    }
                }
            }
        }

        throw new ClassNotFoundException( name );
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        String path = name.replace( '.', '/' ).concat( ".class" );
        JarEntry entry = jar.getJarEntry( path );

        if ( entry != null )
        {
            byte[] classBytes;

            try ( InputStream is = jar.getInputStream( entry ) )
            {
                classBytes = ByteStreams.toByteArray( is );
            } catch ( IOException ex )
            {
                throw new ClassNotFoundException( name, ex );
            }

            int dot = name.lastIndexOf( '.' );
            if ( dot != -1 )
            {
                String pkgName = name.substring( 0, dot );
                if ( getPackage( pkgName ) == null )
                {
                    try
                    {
                        if ( manifest != null )
                        {
                            definePackage( pkgName, manifest, url );
                        } else
                        {
                            definePackage( pkgName, null, null, null, null, null, null, null );
                        }
                    } catch ( IllegalArgumentException ex )
                    {
                        if ( getPackage( pkgName ) == null )
                        {
                            throw new IllegalStateException( "Cannot find package " + pkgName );
                        }
                    }
                }
            }

            CodeSigner[] signers = entry.getCodeSigners();
            CodeSource source = new CodeSource( url, signers );

            return defineClass( name, classBytes, 0, classBytes.length, source );
        }

        return super.findClass( name );
    }

    @Override
    public PluginDescription getDescription()
    {
        return desc;
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            super.close();
        } finally
        {
            jar.close();
        }
    }

    @Override
    void init(Plugin plugin)
    {
        Preconditions.checkArgument( plugin != null, "plugin" );
        Preconditions.checkArgument( plugin.getClass().getClassLoader() == this, "Plugin has incorrect ClassLoader" );
        if ( this.plugin != null )
        {
            throw new IllegalArgumentException( "Plugin already initialized!" );
        }

        this.plugin = plugin;
        plugin.init( proxy, desc, lookup );
    }
}
