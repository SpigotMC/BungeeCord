package net.md_5.bungee.chat;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TranslationRegistry
{

    public static final TranslationRegistry INSTANCE = new TranslationRegistry();
    //
    private final List<TranslationProvider> providers = new LinkedList<>();

    static
    {
        try
        {
            INSTANCE.addProvider( new JsonProvider( "/assets/minecraft/lang/en_us.json" ) );
        } catch ( Exception ex )
        {
        }

        try
        {
            INSTANCE.addProvider( new JsonProvider( "/mojang-translations/en_us.json" ) );
        } catch ( Exception ex )
        {
        }

        try
        {
            INSTANCE.addProvider( new ResourceBundleProvider( "mojang-translations/en_US" ) );
        } catch ( Exception ex )
        {
        }
    }

    private void addProvider(TranslationProvider provider)
    {
        providers.add( provider );
    }

    public String translate(String s)
    {
        for ( TranslationProvider provider : providers )
        {
            String translation = provider.translate( s );

            if ( translation != null )
            {
                return translation;
            }
        }

        return s;
    }

    private interface TranslationProvider
    {

        String translate(String s);
    }

    @Data
    private static class ResourceBundleProvider implements TranslationProvider
    {

        private final ResourceBundle bundle;

        public ResourceBundleProvider(String bundlePath)
        {
            this.bundle = ResourceBundle.getBundle( bundlePath );
        }

        @Override
        public String translate(String s)
        {
            return ( bundle.containsKey( s ) ) ? bundle.getString( s ) : null;
        }
    }

    @Data
    @ToString(exclude = "translations")
    private static class JsonProvider implements TranslationProvider
    {

        private final Map<String, String> translations = new HashMap<>();

        public JsonProvider(String resourcePath) throws IOException
        {
            try ( InputStreamReader rd = new InputStreamReader( JsonProvider.class.getResourceAsStream( resourcePath ), Charsets.UTF_8 ) )
            {
                JsonObject obj = new Gson().fromJson( rd, JsonObject.class );
                for ( Map.Entry<String, JsonElement> entries : obj.entrySet() )
                {
                    translations.put( entries.getKey(), entries.getValue().getAsString() );
                }
            }
        }

        @Override
        public String translate(String s)
        {
            return translations.get( s );
        }
    }
}
