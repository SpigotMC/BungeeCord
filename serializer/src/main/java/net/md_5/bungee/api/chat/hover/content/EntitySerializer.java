package net.md_5.bungee.api.chat.hover.content;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.BaseComponentSerializer;
import net.md_5.bungee.chat.VersionedComponentSerializer;

public class EntitySerializer extends BaseComponentSerializer implements JsonSerializer<Entity>, JsonDeserializer<Entity>
{

    public EntitySerializer(VersionedComponentSerializer serializer)
    {
        super( serializer );
    }

    @Override
    public Entity deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject value = element.getAsJsonObject();

        boolean newEntity = value.has( "uuid" );

        String idString;
        JsonElement uuid = value.get( newEntity ? "uuid" : "id" );
        if ( uuid.isJsonArray() )
        {
            idString = parseUUID( context.deserialize( uuid, int[].class ) ).toString();
        } else
        {
            idString = uuid.getAsString();
        }

        return new Entity(
                ( value.has( newEntity ? "id" : "type" ) ) ? value.get( newEntity ? "id" : "type" ).getAsString() : null,
                idString,
                ( value.has( "name" ) ) ? context.deserialize( value.get( "name" ), BaseComponent.class ) : null
        );
    }

    @Override
    public JsonElement serialize(Entity content, Type type, JsonSerializationContext context)
    {
        JsonObject object = new JsonObject();

        switch ( serializer.getVersion() )
        {
            case V1_21_5:
                object.addProperty( "id", ( content.getType() != null ) ? content.getType() : "minecraft:pig" );
                object.addProperty( "uuid", content.getId() );
                break;
            case V1_16:
                object.addProperty( "type", ( content.getType() != null ) ? content.getType() : "minecraft:pig" );
                object.addProperty( "id", content.getId() );
                break;
            default:
                throw new IllegalArgumentException( "Unknown version " + serializer.getVersion() );
        }

        if ( content.getName() != null )
        {
            object.add( "name", context.serialize( content.getName() ) );
        }
        return object;
    }

    private static UUID parseUUID(int[] array)
    {
        return new UUID( (long) array[0] << 32 | (long) array[1] & 0XFFFFFFFFL, (long) array[2] << 32 | (long) array[3] & 0XFFFFFFFFL );
    }
}
