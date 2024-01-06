package net.md_5.bungee.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.lang.reflect.Type;
import java.util.Set;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentStyle;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.ScoreComponent;
import net.md_5.bungee.api.chat.SelectorComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.EntitySerializer;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.ItemSerializer;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.chat.hover.content.TextSerializer;

public class ComponentSerializer implements JsonDeserializer<BaseComponent>
{

    private static final Gson gson = new GsonBuilder().
            registerTypeAdapter( BaseComponent.class, new ComponentSerializer() ).
            registerTypeAdapter( TextComponent.class, new TextComponentSerializer() ).
            registerTypeAdapter( TranslatableComponent.class, new TranslatableComponentSerializer() ).
            registerTypeAdapter( KeybindComponent.class, new KeybindComponentSerializer() ).
            registerTypeAdapter( ScoreComponent.class, new ScoreComponentSerializer() ).
            registerTypeAdapter( SelectorComponent.class, new SelectorComponentSerializer() ).
            registerTypeAdapter( ComponentStyle.class, new ComponentStyleSerializer() ).
            registerTypeAdapter( Entity.class, new EntitySerializer() ).
            registerTypeAdapter( Text.class, new TextSerializer() ).
            registerTypeAdapter( Item.class, new ItemSerializer() ).
            registerTypeAdapter( ItemTag.class, new ItemTag.Serializer() ).
            create();

    public static final ThreadLocal<Set<BaseComponent>> serializedComponents = new ThreadLocal<Set<BaseComponent>>();

    /**
     * Parse a JSON-compliant String as an array of base components. The input
     * can be one of either an array of components, or a single component
     * object. If the input is an array, each component will be parsed
     * individually and returned in the order that they were parsed. If the
     * input is a single component object, a single-valued array with the
     * component will be returned.
     * <p>
     * <strong>NOTE:</strong> {@link #deserialize(String)} is preferred as it
     * will parse only one component as opposed to an array of components which
     * is non- standard behavior. This method is still appropriate for parsing
     * multiple components at once, although such use case is rarely (if at all)
     * exhibited in vanilla Minecraft.
     *
     * @param json the component json to parse
     * @return an array of all parsed components
     */
    public static BaseComponent[] parse(String json)
    {
        JsonElement jsonElement = JsonParser.parseString( json );

        if ( jsonElement.isJsonArray() )
        {
            return gson.fromJson( jsonElement, BaseComponent[].class );
        } else
        {
            return new BaseComponent[]
            {
                gson.fromJson( jsonElement, BaseComponent.class )
            };
        }
    }

    /**
     * Deserialize a JSON-compliant String as a single component.
     *
     * @param json the component json to parse
     * @return the deserialized component
     * @throws IllegalArgumentException if anything other than a valid JSON
     * component string is passed as input
     */
    public static BaseComponent deserialize(String json)
    {
        JsonElement jsonElement = JsonParser.parseString( json );

        return deserialize( jsonElement );
    }

    /**
     * Deserialize a JSON element as a single component.
     *
     * @param jsonElement the component json to parse
     * @return the deserialized component
     * @throws IllegalArgumentException if anything other than a valid JSON
     * component is passed as input
     */
    public static BaseComponent deserialize(JsonElement jsonElement)
    {
        if ( jsonElement instanceof JsonPrimitive )
        {
            JsonPrimitive primitive = (JsonPrimitive) jsonElement;
            if ( primitive.isString() )
            {
                return new TextComponent( primitive.getAsString() );
            }
        } else if ( jsonElement instanceof JsonArray )
        {
            BaseComponent[] array = gson.fromJson( jsonElement, BaseComponent[].class );
            return TextComponent.fromArray( array );
        }

        return gson.fromJson( jsonElement, BaseComponent.class );
    }

    /**
     * Deserialize a JSON-compliant String as a component style.
     *
     * @param json the component style json to parse
     * @return the deserialized component style
     * @throws IllegalArgumentException if anything other than a valid JSON
     * component style string is passed as input
     */
    public static ComponentStyle deserializeStyle(String json)
    {
        JsonElement jsonElement = JsonParser.parseString( json );

        return deserializeStyle( jsonElement );
    }

    /**
     * Deserialize a JSON element as a component style.
     *
     * @param jsonElement the component style json to parse
     * @return the deserialized component style
     * @throws IllegalArgumentException if anything other than a valid JSON
     * component style is passed as input
     */
    public static ComponentStyle deserializeStyle(JsonElement jsonElement)
    {
        return gson.fromJson( jsonElement, ComponentStyle.class );
    }

    public static JsonElement toJson(BaseComponent component)
    {
        return gson.toJsonTree( component );
    }

    public static JsonElement toJson(ComponentStyle style)
    {
        return gson.toJsonTree( style );
    }

    public static String toString(Object object)
    {
        return gson.toJson( object );
    }

    public static String toString(BaseComponent component)
    {
        return gson.toJson( component );
    }

    public static String toString(BaseComponent... components)
    {
        if ( components.length == 1 )
        {
            return gson.toJson( components[0] );
        } else
        {
            return gson.toJson( new TextComponent( components ) );
        }
    }

    public static String toString(ComponentStyle style)
    {
        return gson.toJson( style );
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
        if ( object.has( "keybind" ) )
        {
            return context.deserialize( json, KeybindComponent.class );
        }
        if ( object.has( "score" ) )
        {
            return context.deserialize( json, ScoreComponent.class );
        }
        if ( object.has( "selector" ) )
        {
            return context.deserialize( json, SelectorComponent.class );
        }
        return context.deserialize( json, TextComponent.class );
    }
}
