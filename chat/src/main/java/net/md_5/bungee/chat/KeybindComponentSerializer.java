package net.md_5.bungee.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.md_5.bungee.api.chat.KeybindComponent;

public class KeybindComponentSerializer extends BaseComponentSerializer implements JsonSerializer<KeybindComponent>, JsonDeserializer<KeybindComponent>
{

    @Override
    public KeybindComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = json.getAsJsonObject();
        if ( !object.has( "keybind" ) )
        {
            throw new JsonParseException( "Could not parse JSON: missing 'keybind' property" );
        }
        KeybindComponent component = new KeybindComponent();
        deserialize( object, component, context );
        component.setKeybind( object.get( "keybind" ).getAsString() );
        return component;
    }

    @Override
    public JsonElement serialize(KeybindComponent src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject object = new JsonObject();
        serialize( object, src, context );
        object.addProperty( "keybind", src.getKeybind() );
        return object;
    }
}
