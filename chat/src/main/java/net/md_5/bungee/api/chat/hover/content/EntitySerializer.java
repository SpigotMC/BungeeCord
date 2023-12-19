package net.md_5.bungee.api.chat.hover.content;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.UUID;
import lombok.NonNull;
import net.md_5.bungee.api.chat.BaseComponent;

public class EntitySerializer implements JsonSerializer<Entity>, JsonDeserializer<Entity>
{

    @NonNull
    private String deserializeUuid(JsonObject object)
    {
        JsonElement uuidElement = object.get( "id" );
        if ( uuidElement.isJsonArray() )
        {
            JsonArray array = uuidElement.getAsJsonArray();
            Preconditions.checkState( array.size() == 4, "UUID don't have array size of 4 (size: " + array.size() + ")" );
            int byte0 = array.get( 0 ).getAsInt();
            int byte1 = array.get( 1 ).getAsInt();
            int byte2 = array.get( 2 ).getAsInt();
            int byte3 = array.get( 3 ).getAsInt();
            return new UUID(
                    (long) byte0 << 32 | ( (long) byte1 & 0xffffffffL ),
                    (long) byte2 << 32 | ( (long) byte3 & 0xffffffffL )
            ).toString();
        } else
        {
            return uuidElement.getAsString();
        }
    }

    @Override
    public Entity deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject value = element.getAsJsonObject();
        return new Entity(
                ( value.has( "type" ) ) ? value.get( "type" ).getAsString() : null,
                deserializeUuid( value ),
                ( value.has( "name" ) ) ? context.deserialize( value.get( "name" ), BaseComponent.class ) : null
        );
    }

    @Override
    public JsonElement serialize(Entity content, Type type, JsonSerializationContext context)
    {
        JsonObject object = new JsonObject();
        object.addProperty( "type", ( content.getType() != null ) ? content.getType() : "minecraft:pig" );
        object.addProperty( "id", content.getId() );
        if ( content.getName() != null )
        {
            object.add( "name", context.serialize( content.getName() ) );
        }
        return object;
    }
}
