package net.md_5.bungee.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.md_5.bungee.api.chat.AbstractBaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import java.lang.reflect.Type;
import java.util.HashSet;

public class ComponentSerializer implements JsonDeserializer<AbstractBaseComponent>
{

    private final static Gson gson = new GsonBuilder().
            registerTypeAdapter( AbstractBaseComponent.class, new ComponentSerializer() ).
            registerTypeAdapter( TextComponent.class, new TextComponentSerializer() ).
            registerTypeAdapter( TranslatableComponent.class, new TranslatableComponentSerializer() ).
            create();

    public final static ThreadLocal<HashSet<AbstractBaseComponent>> serializedComponents = new ThreadLocal<HashSet<AbstractBaseComponent>>();

    public static AbstractBaseComponent[] parse(String json)
    {
        if ( json.startsWith( "[" ) )
        { //Array
            return gson.fromJson( json, AbstractBaseComponent[].class );
        }
        return new AbstractBaseComponent[]
        {
            gson.fromJson( json, AbstractBaseComponent.class )
        };
    }

    public static String toString(AbstractBaseComponent component)
    {
        return gson.toJson( component );
    }

    public static String toString(AbstractBaseComponent... components)
    {
        return gson.toJson( new TextComponent( components ) );
    }

    @Override
    public AbstractBaseComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if ( json.isJsonPrimitive() )
        {
            return new TextComponent( json.getAsString() );
        }
        JsonObject object = json.getAsJsonObject();
        if ( object.has( "translate" ) )
        {
            return context.deserialize( json, TranslatableComponent.class );
        }
        return context.deserialize( json, TextComponent.class );
    }
}
