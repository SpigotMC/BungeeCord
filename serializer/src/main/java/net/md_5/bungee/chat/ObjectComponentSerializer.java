package net.md_5.bungee.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.md_5.bungee.api.chat.ObjectComponent;

import java.lang.reflect.Type;

public class ObjectComponentSerializer extends BaseComponentSerializer implements JsonSerializer<ObjectComponent>, JsonDeserializer<ObjectComponent>
{

    public ObjectComponentSerializer(VersionedComponentSerializer serializer)
    {
        super( serializer );
    }

    @Override
    public ObjectComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = json.getAsJsonObject();

        String player = object.has( "player" ) ? object.get( "player" ).getAsString() : null;
        String sprite = object.has( "sprite" ) ? object.get( "sprite" ).getAsString() : null;
        String atlas = object.has( "atlas" ) ? object.get( "atlas" ).getAsString() : null;

        if ( player == null && sprite == null )
        {
            throw new JsonParseException( "Could not parse JSON: missing 'player' or 'sprite' property" );
        }
        ObjectComponent objectComponent = new ObjectComponent( player, atlas, sprite );
        deserialize( object, objectComponent, context );
        return objectComponent;
    }

    @Override
    public JsonElement serialize(ObjectComponent src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject object = new JsonObject();
        serialize( object, src, context );

        if ( src.getSprite() != null )
        {
            object.addProperty( "sprite", src.getSprite() );
        }
        if (src.getAtlas() != null)
        {
            object.addProperty( "atlas", src.getAtlas() );
        }
        if ( src.getPlayer() != null )
        {
            object.addProperty( "player", src.getPlayer() );
        }
        return object;
    }
}
