package net.md_5.bungee.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import java.lang.reflect.Type;
import java.util.HashSet;

public class ComponentSerializer implements JsonDeserializer<BaseComponent>
{

    private final static Gson gson = new GsonBuilder().
            registerTypeAdapter( BaseComponent.class, new ComponentSerializer() ).
            registerTypeAdapter( TextComponent.class, new TextComponentSerializer() ).
            registerTypeAdapter( TranslatableComponent.class, new TranslatableComponentSerializer() ).
            create();

    public final static ThreadLocal<HashSet<BaseComponent>> serializedComponents = new ThreadLocal<HashSet<BaseComponent>>();

    public static BaseComponent[] parse(String json)
    {
        if ( json.startsWith( "[" ) )
        { //Array
            return gson.fromJson( json, BaseComponent[].class );
        }
        return new BaseComponent[]
        {
            gson.fromJson( json, BaseComponent.class )
        };
    }

    public static String toString(BaseComponent component)
    {
        return gson.toJson( component );
    }

    public static String toString(BaseComponent... components)
    {
        return gson.toJson( new TextComponent( components ) );
    }

    @Override
    public BaseComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
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
