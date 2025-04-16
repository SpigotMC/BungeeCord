package net.md_5.bungee.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.awt.Color;
import java.lang.reflect.Type;
import java.util.Map;
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
        if ( style.hasColor() && style.getColor().getColor() != null )
        {
            object.addProperty( "color", style.getColor().getName() );
        }
        if ( style.hasShadowColor() )
        {
            object.addProperty( "shadow_color", style.getShadowColor().getRGB() );
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
        for ( Map.Entry<String, JsonElement> entry : object.entrySet() )
        {
            String name = entry.getKey();
            JsonElement value = entry.getValue();
            switch ( name )
            {
                case "bold":
                    builder.bold( getAsBoolean( value ) );
                    break;
                case "italic":
                    builder.italic( getAsBoolean( value ) );
                    break;
                case "underlined":
                    builder.underlined( getAsBoolean( value ) );
                    break;
                case "strikethrough":
                    builder.strikethrough( getAsBoolean( value ) );
                    break;
                case "obfuscated":
                    builder.obfuscated( getAsBoolean( value ) );
                    break;
                case "color":
                    builder.color( ChatColor.of( value.getAsString() ) );
                    break;
                case "shadow_color":
                    if ( value.isJsonArray() )
                    {
                        JsonArray array = value.getAsJsonArray();

                        builder.shadowColor( new Color( array.get( 0 ).getAsFloat(), array.get( 1 ).getAsFloat(), array.get( 2 ).getAsFloat(), array.get( 3 ).getAsFloat() ) );
                    } else if ( value.isJsonPrimitive() )
                    {
                        builder.shadowColor( new Color( value.getAsNumber().intValue(), true ) );
                    }
                    break;
                case "font":
                    builder.font( value.getAsString() );
                    break;
            }
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
