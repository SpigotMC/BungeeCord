package net.md_5.bungee.chat;

import com.google.gson.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.Keybind;
import net.md_5.bungee.api.chat.KeybindComponent;

import java.lang.reflect.Type;
import java.util.List;

public class KeybindComponentSerializer extends BaseComponentSerializer implements JsonSerializer<KeybindComponent>, JsonDeserializer<KeybindComponent>
{

    @Override
    public KeybindComponent deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException
    {
        KeybindComponent component = new KeybindComponent();
        JsonObject object = json.getAsJsonObject();
        deserialize( object, component, context );
        component.setKeybind( Keybind.deserialize( object.get( "keybind" ).getAsString().toUpperCase() ) );
        return component;
    }

    @Override
    public JsonElement serialize( KeybindComponent src, Type typeOfSrc, JsonSerializationContext context )
    {
        List<BaseComponent> extra = src.getExtra();
        JsonObject object = new JsonObject();
        if ( src.hasFormatting() || ( extra != null && !extra.isEmpty() ) ) {
            serialize( object, src, context );
        }
        object.addProperty( "keybind", src.getKeybind().getKeyCode() );
        return object;
    }
}
