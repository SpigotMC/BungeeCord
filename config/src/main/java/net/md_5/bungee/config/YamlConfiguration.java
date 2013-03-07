package net.md_5.bungee.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class YamlConfiguration extends ConfigurationProvider
{

    private final ThreadLocal<Yaml> yaml = new ThreadLocal<Yaml>()
    {
        @Override
        protected Yaml initialValue()
        {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );
            return new Yaml( options );
        }
    };

    @Override
    public Configuration load(File file)
    {
        try ( FileReader reader = new FileReader( file ) )
        {
            return load( reader );
        } catch ( IOException ex )
        {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(Reader reader)
    {
        Configuration conf = new Configuration( (Map<String, Object>) yaml.get().loadAs( reader, Map.class ), null );
        return conf;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(String string)
    {
        Configuration conf = new Configuration( (Map<String, Object>) yaml.get().loadAs( string, Map.class ), null );
        return conf;
    }
}
