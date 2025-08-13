package net.md_5.bungee.api.chat.hover.content;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.md_5.bungee.api.chat.ItemTag;

public class ItemSerializer implements JsonSerializer<Item>, JsonDeserializer<Item>
{
    private static final String ID_KEY = "id";
    private static final String COMPONENTS_KEY = "components";
    private static final String TAG_KEY = "tag";
    // Count must be serialized as "count" for >= 1.20.5
    // See: https://minecraft.wiki/w/Data_component_format#Usage
    private static final String COUNT_KEY = "count";
    // Count Must be serialized as "Count" for < 1.20.5
    // See: https://minecraft.wiki/w/Item_format/Before_1.20.5#NBT_structure
    private static final String LEGACY_COUNT_KEY = "Count";

    @Override
    public Item deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject value = element.getAsJsonObject();

        int count = -1;
        JsonPrimitive countObj = null;
        if ( value.has( COUNT_KEY ) )
        {
            countObj = value.get( COUNT_KEY ).getAsJsonPrimitive();
        } else if ( value.has( LEGACY_COUNT_KEY ) )
        {
            countObj = value.get( LEGACY_COUNT_KEY ).getAsJsonPrimitive();
        }
        if ( countObj != null )
        {
            if ( countObj.isNumber() )
            {
                count = countObj.getAsInt();
            } else if ( countObj.isString() )
            {
                String cString = countObj.getAsString();
                char last = cString.charAt( cString.length() - 1 );
                // Check for all number suffixes
                if ( last == 'b' || last == 's' || last == 'l' || last == 'f' || last == 'd' )
                {
                    cString = cString.substring( 0, cString.length() - 1 );
                }
                try
                {
                    count = Integer.parseInt( cString );
                } catch ( NumberFormatException ex )
                {
                    throw new JsonParseException( "Could not parse count: " + ex );
                }
            }
        }

        return new Item(
                ( value.has( ID_KEY ) ) ? value.get( ID_KEY ).getAsString() : null,
                count,
                ( value.has( COMPONENTS_KEY ) ) ? value.get( COMPONENTS_KEY ) : null,
                ( value.has( TAG_KEY ) ) ? context.deserialize( value.get( TAG_KEY ), ItemTag.class ) : null
        );
    }

    @Override
    public JsonElement serialize(Item content, Type type, JsonSerializationContext context)
    {
        boolean isLegacy = content.getTag() != null;

        JsonObject object = new JsonObject();
        // Default to air for legacy, dirt for modern (air is no longer allowed)
        String idDefault = isLegacy ? "minecraft:air" : "minecraft:dirt";
        object.addProperty( ID_KEY, ( content.getId() == null ) ? idDefault : content.getId() );

        if ( content.getCount() != -1 )
        {
            object.addProperty( isLegacy ? LEGACY_COUNT_KEY : COUNT_KEY, content.getCount() );
        }
        // Item Components system
        if ( content.getComponents() != null )
        {
            object.add( COMPONENTS_KEY, content.getComponents() );
        }
        // Legacy NBT Tag system
        if ( content.getTag() != null )
        {
            object.add( TAG_KEY, context.serialize( content.getTag() ) );
        }
        return object;
    }
}
