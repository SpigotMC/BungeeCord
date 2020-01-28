package net.md_5.bungee.config;

import java.io.*;

@Deprecated
class YamlConfigurationProvider extends ConfigurationProvider
{
    @Override
    public void save(Configuration config, File file) throws IOException
    {
        config.save( file );
    }

    @Override
    public void save(Configuration config, Writer writer)
    {
        config.save( writer );
    }

    @Override
    public Configuration load(File file) throws IOException
    {
        return new YamlConfiguration().load( file );
    }

    @Override
    public Configuration load(File file, Configuration defaults) throws IOException
    {
        return new YamlConfiguration().load( file ).setDefaults( defaults );
    }

    @Override
    public Configuration load(Reader reader)
    {
        try
        {
            return new YamlConfiguration().load( reader );
        } catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public Configuration load(Reader reader, Configuration defaults)
    {
        try
        {
            return new YamlConfiguration().load( reader ).setDefaults( defaults );
        } catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public Configuration load(InputStream is)
    {
        try
        {
            return new YamlConfiguration().load( is );
        } catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public Configuration load(InputStream is, Configuration defaults)
    {
        try
        {
            return new YamlConfiguration().load( is ).setDefaults( defaults );
        } catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public Configuration load(String string)
    {
        return new YamlConfiguration().load( string );
    }

    @Override
    public Configuration load(String string, Configuration defaults)
    {
        return new YamlConfiguration().load( string ).setDefaults( defaults );
    }
}
