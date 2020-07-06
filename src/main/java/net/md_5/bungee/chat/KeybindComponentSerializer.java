package net.md_5.bungee.chat;

import com.google.gson.*;
import net.md_5.bungee.api.chat.KeybindComponent;

import java.lang.reflect.Type;

public class KeybindComponentSerializer extends BaseComponentSerializer implements JsonSerializer<KeybindComponent>, JsonDeserializer<KeybindComponent> {

    @Override
    public KeybindComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        KeybindComponent component = new KeybindComponent();
        JsonObject object = json.getAsJsonObject();
        deserialize(object, component, context);
        component.setKeybind(object.get("keybind").getAsString());
        return component;
    }

    @Override
    public JsonElement serialize(KeybindComponent src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        serialize(object, src, context);
        object.addProperty("keybind", src.getKeybind());
        return object;
    }
}
