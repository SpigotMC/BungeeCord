package net.md_5.bungee.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated This class was merged into {@link Configuration}.
 * To load a new configuration, instantiate the implementation directly.
 * To save an existing configuration, use one of the save methods in the {@link Configuration} class.
 *
 * @see Configuration
 * @see Configuration#save(File)
 * @see Configuration#save(Writer)
 */
@Deprecated
public abstract class ConfigurationProvider
{

    private static final Map<Class<?>, ConfigurationProvider> providers = new HashMap<>();

    static
    {
        final YamlConfigurationProvider provider = new YamlConfigurationProvider();
        providers.put( YamlConfiguration.class, provider);
        providers.put( YamlConfigurationProvider.class, provider);
    }

    /**
     * @deprecated Instantiate the implementation of {@link Configuration} directly.
     */
    @Deprecated
    public static ConfigurationProvider getProvider(Class<?> provider)
    {
        return providers.get( provider );
    }

    /*------------------------------------------------------------------------*/

    /**
     * @deprecated This method was merged into the {@link Configuration} class.
     *
     * @see Configuration#save(File)
     */
    @Deprecated
    public abstract void save(Configuration config, File file) throws IOException;

    /**
     * @deprecated This method was merged into the {@link Configuration} class.
     *
     * @see Configuration#save(Writer)
     */
    @Deprecated
    public abstract void save(Configuration config, Writer writer);

    /**
     * @deprecated This method was merged into the {@link Configuration} class.
     *
     * @see Configuration#load(File)
     */
    @Deprecated
    public abstract Configuration load(File file) throws IOException;

    /**
     * @deprecated This method was merged into the {@link Configuration} class.
     *
     * @see Configuration#load(File)
     * @see Configuration#setDefaults(Configuration)
     */
    @Deprecated
    public abstract Configuration load(File file, Configuration defaults) throws IOException;

    /**
     * @deprecated This method was merged into the {@link Configuration} class.
     *
     * @see Configuration#load(Reader)
     */
    @Deprecated
    public abstract Configuration load(Reader reader);

    /**
     * @deprecated This method was merged into the {@link Configuration} class.
     *
     * @see Configuration#load(Reader)
     * @see Configuration#setDefaults(Configuration)
     */
    @Deprecated
    public abstract Configuration load(Reader reader, Configuration defaults);

    /**
     * @deprecated This method was merged into the {@link Configuration} class.
     *
     * @see Configuration#load(InputStream)
     */
    @Deprecated
    public abstract Configuration load(InputStream is);

    /**
     * @deprecated This method was merged into the {@link Configuration} class.
     *
     * @see Configuration#load(InputStream)
     * @see Configuration#setDefaults(Configuration)
     */
    @Deprecated
    public abstract Configuration load(InputStream is, Configuration defaults);

    /**
     * @deprecated This method was merged into the {@link Configuration} class.
     *
     * @see Configuration#load(String)
     */
    @Deprecated
    public abstract Configuration load(String string);

    /**
     * @deprecated This method was merged into the {@link Configuration} class.
     *
     * @see Configuration#load(String)
     * @see Configuration#setDefaults(Configuration)
     */
    @Deprecated
    public abstract Configuration load(String string, Configuration defaults);
}
