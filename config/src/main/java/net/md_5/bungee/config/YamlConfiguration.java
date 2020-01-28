package net.md_5.bungee.config;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import com.google.common.base.Charsets;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

@NoArgsConstructor()
public class YamlConfiguration extends Configuration
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
    @SuppressWarnings("unchecked")
    public Configuration load(Reader reader) throws IOException
    {
        Map<String, Object> loadedData;
        try
        {
            loadedData = yaml.get().loadAs( reader, LinkedHashMap.class );
        } catch ( YAMLException e )
        {
            final Throwable cause = e.getCause();
            if ( cause instanceof IOException ) //unwrap IOExceptions
            {
                throw ( (IOException) cause );
            }
            throw e;
        }
        if ( loadedData != null )
        {
            load( loadedData );
        }
        return this;
    }

    @Override
    public void save(File file) throws IOException
    {
        try ( Writer writer = new OutputStreamWriter( new FileOutputStream( file ), Charsets.UTF_8 ) )
        {
            save( writer );
        }
    }

    @Override
    public void save(Writer writer)
    {
        yaml.get().dump( self, writer );
    }
}
