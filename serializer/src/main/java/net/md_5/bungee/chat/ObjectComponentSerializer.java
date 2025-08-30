package net.md_5.bungee.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.UUID;
import net.md_5.bungee.api.chat.ObjectComponent;
import net.md_5.bungee.api.chat.objects.PlayerObject;
import net.md_5.bungee.api.chat.objects.SpriteObject;
import net.md_5.bungee.api.chat.player.Profile;
import net.md_5.bungee.api.chat.player.Property;

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

        String sprite = object.has( "sprite" ) ? object.get( "sprite" ).getAsString() : null;
        String atlas = object.has( "atlas" ) ? object.get( "atlas" ).getAsString() : null;

        if ( sprite != null )
        {
            ObjectComponent component = new ObjectComponent( new SpriteObject( atlas, sprite ) );
            deserialize( object, component, context );
            return component;
        }
        JsonElement player = object.get( "player" );
        if ( player != null )
        {
            String name = null;
            UUID uuid = null;
            Property[] properties = null;
            Boolean hat = object.has( "hat" ) ? object.get( "hat" ).getAsBoolean() : null;
            if ( player.isJsonObject() )
            {
                JsonObject playerObj = player.getAsJsonObject();
                validateName( name = playerObj.has( "name" ) ? playerObj.get( "name" ).getAsString() : null );
                uuid = playerObj.has( "id" ) ? parseUUID( context.deserialize( playerObj.get( "id" ), int[].class ) ) : null;
                properties = playerObj.has( "properties" ) ? context.deserialize( playerObj.get( "properties" ), Property[].class ) : null;
            } else if ( player.isJsonPrimitive() )
            {
                validateName( name = player.getAsString() );
            }
            ObjectComponent component = new ObjectComponent( new PlayerObject( new Profile( name, uuid, properties ), hat ) );
            deserialize( object, component, context );
            return component;
        }

        throw new JsonParseException( "Could not parse JSON: missing 'player' or 'sprite' property" );
    }

    @Override
    public JsonElement serialize(ObjectComponent src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject object = new JsonObject();
        serialize( object, src, context );

        if ( src.getObject() instanceof SpriteObject )
        {
            SpriteObject sprite = (SpriteObject) src.getObject();
            object.addProperty( "sprite", sprite.getSprite() );
            if ( sprite.getAtlas() != null )
            {
                object.addProperty( "atlas", sprite.getAtlas() );
            }
            return object;
        }

        if ( src.getObject() instanceof PlayerObject )
        {
            PlayerObject player = (PlayerObject) src.getObject();

            if ( player.getHat() != null )
            {
                object.addProperty( "hat", player.getHat() );
            }

            JsonObject playerObj = new JsonObject();
            Profile profile = player.getProfile();

            if ( profile.getName() != null )
            {
                playerObj.addProperty( "name", profile.getName() );
            }

            if ( profile.getUuid() != null )
            {
                int[] uuidArray = new int[4];
                long most = profile.getUuid().getMostSignificantBits();
                long least = profile.getUuid().getLeastSignificantBits();
                uuidArray[0] = (int) ( most >> 32 );
                uuidArray[1] = (int) most;
                uuidArray[2] = (int) ( least >> 32 );
                uuidArray[3] = (int) least;
                playerObj.add( "id", context.serialize( uuidArray ) );
            }

            if ( profile.getProperties() != null )
            {
                playerObj.add( "properties", context.serialize( profile.getProperties(), Property[].class ) );
            }
            object.add( "player", playerObj );
            return object;
        }

        throw new JsonParseException( "Could not serialize ObjectComponent: unknown object type " + src.getObject().getClass() );
    }

    private static UUID parseUUID(int[] array)
    {
        if ( array.length != 4 )
        {
            throw new JsonParseException( "UUID integer array must be exactly 4 integers long" );
        }
        return new UUID( (long) array[0] << 32 | (long) array[1] & 0XFFFFFFFFL, (long) array[2] << 32 | (long) array[3] & 0XFFFFFFFFL );
    }

    private static void validateName(String name)
    {
        if ( name != null && ( name.length() > 16 || name.isEmpty() ) )
        {
            throw new JsonParseException( "Could not parse JSON: player name must be 16 characters or fewer and not empty" );
        }
    }
}
