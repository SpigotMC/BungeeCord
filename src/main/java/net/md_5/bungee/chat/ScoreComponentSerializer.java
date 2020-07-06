package net.md_5.bungee.chat;

import com.google.gson.*;
import net.md_5.bungee.api.chat.ScoreComponent;

import java.lang.reflect.Type;

public class ScoreComponentSerializer extends BaseComponentSerializer implements JsonSerializer<ScoreComponent>, JsonDeserializer<ScoreComponent> {

    @Override
    public ScoreComponent deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = element.getAsJsonObject();
        JsonObject score = json.get("score").getAsJsonObject();
        if (!score.has("name") || !score.has("objective")) {
            throw new JsonParseException("A score component needs at least a name and an objective");
        }

        String name = score.get("name").getAsString();
        String objective = score.get("objective").getAsString();
        ScoreComponent component = new ScoreComponent(name, objective);
        if (score.has("value") && !score.get("value").getAsString().isEmpty()) {
            component.setValue(score.get("value").getAsString());
        }

        deserialize(json, component, context);
        return component;
    }

    @Override
    public JsonElement serialize(ScoreComponent component, Type type, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        serialize(root, component, context);
        JsonObject json = new JsonObject();
        json.addProperty("name", component.getName());
        json.addProperty("objective", component.getObjective());
        json.addProperty("value", component.getValue());
        root.add("score", json);
        return root;
    }
}
