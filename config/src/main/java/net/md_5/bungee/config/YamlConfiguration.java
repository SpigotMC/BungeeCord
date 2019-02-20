package net.md_5.bungee.config;

import com.google.common.base.Charsets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
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
    public void save(File file) throws IOException {
        try ( Writer writer = new OutputStreamWriter( new FileOutputStream( file ), Charsets.UTF_8 ) )
        {
            save( writer );
        }
    }

    @Override
    public void save(Writer writer) {
        yaml.get().dump( self, writer );
    }


    @Override
    public Configuration load(File file) throws IOException
    {
        try ( FileInputStream is = new FileInputStream( file ) )
        {
            return load( is );
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(Reader reader)
    {
        Map<String, Object> loadedData = yaml.get().loadAs( reader, LinkedHashMap.class );
        if ( loadedData != null )
        {
            load ( loadedData );
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(InputStream is)
    {
        Map<String, Object> loadedData = yaml.get().loadAs( is, LinkedHashMap.class );
        if ( loadedData != null )
        {
            load ( loadedData );
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(String string)
    {
        Map<String, Object> loadedData = yaml.get().loadAs( string, LinkedHashMap.class );
        if ( loadedData != null )
        {
            load ( loadedData );
        }
        return this;
    }
}
