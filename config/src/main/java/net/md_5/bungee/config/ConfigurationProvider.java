package net.md_5.bungee.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public abstract class ConfigurationProvider
{

    private static final Map<Class<? extends ConfigurationProvider>, ConfigurationProvider> providers = new HashMap<>();

    static
    {
        try
        {
            providers.put( YamlConfiguration.class, new YamlConfiguration() );
        } catch ( NoClassDefFoundError ex )
        {
            // Ignore, no SnakeYAML
        }

        try
        {
            providers.put( JsonConfiguration.class, new JsonConfiguration() );
        } catch ( NoClassDefFoundError ex )
        {
            // Ignore, no Gson
        }
    }

    public static ConfigurationProvider getProvider(Class<? extends ConfigurationProvider> provider)
    {
        return providers.get( provider );
    }

    /*------------------------------------------------------------------------*/
    public abstract void save(Configuration config, File file) throws IOException;

    public abstract void save(Configuration config, Writer writer);

    public abstract Configuration load(File file) throws IOException;

    public abstract Configuration load(File file, Configuration defaults) throws IOException;

    public abstract Configuration load(Reader reader);

    public abstract Configuration load(Reader reader, Configuration defaults);

    public abstract Configuration load(InputStream is);

    public abstract Configuration load(InputStream is, Configuration defaults);

    public abstract Configuration load(String string);

    public abstract Configuration load(String string, Configuration defaults);
}
