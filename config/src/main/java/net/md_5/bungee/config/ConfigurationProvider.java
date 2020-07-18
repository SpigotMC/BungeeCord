package net.md_5.bungee.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public abstract void save(@NotNull Configuration config, @NotNull File file) throws IOException;

    public abstract void save(@NotNull Configuration config, @NotNull Writer writer);

    public abstract Configuration load(@NotNull File file) throws IOException;

    public abstract Configuration load(@NotNull File file, @Nullable Configuration defaults) throws IOException;

    public abstract Configuration load(@NotNull Reader reader);

    public abstract Configuration load(@NotNull Reader reader, @Nullable Configuration defaults);

    public abstract Configuration load(@NotNull InputStream is);

    public abstract Configuration load(@NotNull InputStream is, @Nullable Configuration defaults);

    public abstract Configuration load(@NotNull String string);

    public abstract Configuration load(@NotNull String string, @Nullable Configuration defaults);
}
