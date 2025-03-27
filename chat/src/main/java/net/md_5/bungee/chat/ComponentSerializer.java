package net.md_5.bungee.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentStyle;
import net.md_5.bungee.api.chat.hover.content.Content;

public class ComponentSerializer implements JsonDeserializer<BaseComponent>
{

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
        return VersionedComponentSerializer.getDefault().parse( json );
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
        return VersionedComponentSerializer.getDefault().deserialize( json );
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
        return VersionedComponentSerializer.getDefault().deserialize( jsonElement );
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
        return VersionedComponentSerializer.getDefault().deserializeStyle( json );
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
        return VersionedComponentSerializer.getDefault().deserializeStyle( jsonElement );
    }

    public static JsonElement toJson(BaseComponent component)
    {
        return VersionedComponentSerializer.getDefault().toJson( component );
    }

    public static JsonElement toJson(ComponentStyle style)
    {
        return VersionedComponentSerializer.getDefault().toJson( style );
    }

    /**
     * @param object the object to serialize
     * @return the JSON string representation of the object
     * @deprecated Error-prone, be careful which object you input here
     */
    @Deprecated
    public static String toString(Object object)
    {
        return VersionedComponentSerializer.getDefault().toString( object );
    }

    /**
     * @param content the content to serialize
     * @return the JSON string representation of the object
     * @deprecated for legacy internal use only
     */
    @Deprecated
    public static String toString(Content content)
    {
        return VersionedComponentSerializer.getDefault().toString( content );
    }

    public static String toString(BaseComponent component)
    {
        return VersionedComponentSerializer.getDefault().toString( component );
    }

    public static String toString(BaseComponent... components)
    {
        return VersionedComponentSerializer.getDefault().toString( components );
    }

    public static String toString(ComponentStyle style)
    {
        return VersionedComponentSerializer.getDefault().toString( style );
    }

    @Override
    public BaseComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return VersionedComponentSerializer.getDefault().deserialize( json, typeOfT, context );
    }
}
