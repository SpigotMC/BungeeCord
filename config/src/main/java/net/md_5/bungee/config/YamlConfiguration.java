package net.md_5.bungee.server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class YamlConfiguration extends ConfigurationProvider
{

    private final ThreadLocal<Yaml> yaml = new ThreadLocal<Yaml>()
    {
        @Override
        protected Yaml initialValue()
        {
            Representer representer = new Representer()
            {
                {
                    representers.put( Configuration.class, new Represent()
                    {
                        @Override
                        public Node representData(Object data)
                        {
                            return represent( ( (Configuration) data ).self );
                        }
                    } );
                }
            };

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );

            return new Yaml( new Constructor(), representer, options );
        }
    };

    @Override
    public void save(Configuration server, File file) throws IOException
    {
        try ( FileWriter writer = new FileWriter( file ) )
        {
            save( server, writer );
        }
    }

    @Override
    public void save(Configuration server, Writer writer)
    {
        yaml.get().dump( server.self, writer );
    }

    @Override
    public Configuration load(File file) throws IOException
    {
        return load( file, null );
    }

    @Override
    public Configuration load(File file, Configuration defaults) throws IOException
    {
        try ( FileReader reader = new FileReader( file ) )
        {
            return load( reader, defaults );
        }
    }

    @Override
    public Configuration load(Reader reader)
    {
        return load( reader, null );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(Reader reader, Configuration defaults)
    {
        Map<String, Object> map = yaml.get().loadAs( reader, LinkedHashMap.class );
        if ( map == null )
        {
            map = new LinkedHashMap<>();
        }
        return new Configuration( map, defaults );
    }

    @Override
    public Configuration load(InputStream is)
    {
        return load( is, null );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(InputStream is, Configuration defaults)
    {
        Map<String, Object> map = yaml.get().loadAs( is, LinkedHashMap.class );
        if ( map == null )
        {
            map = new LinkedHashMap<>();
        }
        return new Configuration( map, defaults );
    }

    @Override
    public Configuration load(String string)
    {
        return load( string, null );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(String string, Configuration defaults)
    {
        Map<String, Object> map = yaml.get().loadAs( string, LinkedHashMap.class );
        if ( map == null )
        {
            map = new LinkedHashMap<>();
        }
        return new Configuration( map, defaults );
    }
}
