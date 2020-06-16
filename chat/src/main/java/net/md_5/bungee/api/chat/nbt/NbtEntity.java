package net.md_5.bungee.api.chat.nbt;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Used to show entities within the {@link net.md_5.bungee.api.chat.HoverEvent} in the chat api.
 *
 * This creates dummy info and does not require any existing entity on the server for use.
 */
@Data
@NoArgsConstructor
public class NbtEntity
{

    /**
     * Name of the entity. This is optional and hidden if non present.
     */
    private String name;
    /**
     * The type of entity. Should be namespaced entity ID. Present
     * minecraft:pig if invalid.
     */
    private String type;
    /**
     * String containing the UUID of entity in a hyphenated hexadecimal format.
     * This should be a valid UUID.
     */
    private String id;

    /**
     * Serialises this entity into a JSON format recognised by the Minecraft chat.
     */
    public static class Serializer implements JsonSerializer<NbtEntity>, JsonDeserializer<NbtEntity>
    {

        @Override
        public NbtEntity deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
        {
            NbtEntity entity = new NbtEntity();

            JsonObject json = element.getAsJsonObject();

            if ( json.has( "name" ) )
            {
                entity.setName( json.get( "name" ).getAsString() );
            }
            if ( json.has( "type" ) )
            {
                entity.setType( json.get( "type" ).getAsString() );
            }
            if ( json.has( "id" ) )
            {
                entity.setId( json.get( "id" ).getAsString() );
            }
            return entity;
        }

        @Override
        public JsonElement serialize(NbtEntity entity, Type type, JsonSerializationContext context)
        {
            JsonObject root = new JsonObject();
            if ( entity.name != null )
            {
                root.addProperty( "name", entity.name );
            }
            if ( entity.id != null )
            {
                root.addProperty( "id", entity.id );
            }
            if ( entity.type != null )
            {
                root.addProperty( "type", entity.type );
            }
            return root;
        }
    }
}
