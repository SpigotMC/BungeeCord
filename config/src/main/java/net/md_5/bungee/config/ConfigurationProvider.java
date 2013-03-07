package net.md_5.bungee.config;

import java.io.File;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public abstract class ConfigurationProvider
{

    private static final Map<Class<? extends ConfigurationProvider>, ConfigurationProvider> providers = new HashMap<>();

    static
    {
        providers.put( YamlConfiguration.class, new YamlConfiguration() );
    }

    public ConfigurationProvider getProvider(Class<? extends ConfigurationProvider> provider)
    {
        return providers.get( provider );
    }
    /*------------------------------------------------------------------------*/

    public abstract Configuration load(File file);

    public abstract Configuration load(Reader reader);

    public abstract Configuration load(String string);
}
