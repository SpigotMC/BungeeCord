package net.md_5.bungee;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.md_5.bungee.api.ServerPing;

import java.lang.reflect.Type;
import java.util.UUID;

public class PlayerInfoSerializer implements JsonSerializer<ServerPing.PlayerInfo>, JsonDeserializer<ServerPing.PlayerInfo>
{

    private final int protocol;

    public PlayerInfoSerializer(int protocol)
    {
        this.protocol = protocol;
    }

    @Override
    public ServerPing.PlayerInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject js = json.getAsJsonObject();
        ServerPing.PlayerInfo info = new ServerPing.PlayerInfo( js.get( "name" ).getAsString(), (UUID) null );
        String id = js.get( "id" ).getAsString();
        if ( protocol == 4 || !id.contains( "-" ) )
        {
            info.setId( id );
        } else
        {
            info.setUniqueId( UUID.fromString( id ) );
        }
        return info;
    }

    @Override
    public JsonElement serialize(ServerPing.PlayerInfo src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject out = new JsonObject();
        out.addProperty( "name", src.getName() );
        if ( protocol == 4 )
        {
            out.addProperty( "id", src.getId() );
        } else
        {
            out.addProperty( "id", src.getUniqueId().toString() );
        }
        return out;
    }
}
