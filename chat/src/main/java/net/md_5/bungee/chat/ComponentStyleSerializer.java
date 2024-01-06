package net.md_5.bungee.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentStyle;
import net.md_5.bungee.api.chat.ComponentStyleBuilder;

public class ComponentStyleSerializer implements JsonSerializer<ComponentStyle>, JsonDeserializer<ComponentStyle>
{

    private static boolean getAsBoolean(JsonElement el)
    {
        if ( el.isJsonPrimitive() )
        {
            JsonPrimitive primitive = (JsonPrimitive) el;

            if ( primitive.isBoolean() )
            {
                return primitive.getAsBoolean();
            }

            if ( primitive.isNumber() )
            {
                Number number = primitive.getAsNumber();
                if ( number instanceof Byte )
                {
                    return number.byteValue() != 0;
                }
            }
        }

        return false;
    }

    static void serializeTo(ComponentStyle style, JsonObject object)
    {
        if ( style.isBoldRaw() != null )
        {
            object.addProperty( "bold", style.isBoldRaw() );
        }
        if ( style.isItalicRaw() != null )
        {
            object.addProperty( "italic", style.isItalicRaw() );
        }
        if ( style.isUnderlinedRaw() != null )
        {
            object.addProperty( "underlined", style.isUnderlinedRaw() );
        }
        if ( style.isStrikethroughRaw() != null )
        {
            object.addProperty( "strikethrough", style.isStrikethroughRaw() );
        }
        if ( style.isObfuscatedRaw() != null )
        {
            object.addProperty( "obfuscated", style.isObfuscatedRaw() );
        }
        if ( style.hasColor() )
        {
            object.addProperty( "color", style.getColor().getName() );
        }
        if ( style.hasFont() )
        {
            object.addProperty( "font", style.getFont() );
        }
    }

    @Override
    public ComponentStyle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        ComponentStyleBuilder builder = ComponentStyle.builder();
        JsonObject object = json.getAsJsonObject();
        if ( object.has( "bold" ) )
        {
            builder.bold( getAsBoolean( object.get( "bold" ) ) );
        }
        if ( object.has( "italic" ) )
        {
            builder.italic( getAsBoolean( object.get( "italic" ) ) );
        }
        if ( object.has( "underlined" ) )
        {
            builder.underlined( getAsBoolean( object.get( "underlined" ) ) );
        }
        if ( object.has( "strikethrough" ) )
        {
            builder.strikethrough( getAsBoolean( object.get( "strikethrough" ) ) );
        }
        if ( object.has( "obfuscated" ) )
        {
            builder.obfuscated( getAsBoolean( object.get( "obfuscated" ) ) );
        }
        if ( object.has( "color" ) )
        {
            builder.color( ChatColor.of( object.get( "color" ).getAsString() ) );
        }
        if ( object.has( "font" ) )
        {
            builder.font( object.get( "font" ).getAsString() );
        }
        return builder.build();
    }

    @Override
    public JsonElement serialize(ComponentStyle src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject object = new JsonObject();
        serializeTo( src, object );
        return object;
    }
}
