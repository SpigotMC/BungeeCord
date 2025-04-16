package net.md_5.bungee.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.md_5.bungee.api.chat.ScoreComponent;

public class ScoreComponentSerializer extends BaseComponentSerializer implements JsonSerializer<ScoreComponent>, JsonDeserializer<ScoreComponent>
{

    public ScoreComponentSerializer(VersionedComponentSerializer serializer)
    {
        super( serializer );
    }

    @Override
    public ScoreComponent deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject json = element.getAsJsonObject();
        JsonObject score = json.getAsJsonObject( "score" );
        if ( score == null )
        {
            throw new JsonParseException( "Could not parse JSON: missing 'score' property" );
        }
        JsonElement nameJson = score.get( "name" );
        if ( nameJson == null )
        {
            throw new JsonParseException( "A score component needs at least a name (and an objective)" );
        }
        JsonElement objectiveJson = score.get( "objective" );
        if ( objectiveJson == null )
        {
            throw new JsonParseException( "A score component needs at least a name and an objective" );
        }

        String name = nameJson.getAsString();
        String objective = objectiveJson.getAsString();
        ScoreComponent component = new ScoreComponent( name, objective );
        JsonElement value = score.get( "value" );
        if ( value != null && !value.getAsString().isEmpty() )
        {
            component.setValue( value.getAsString() );
        }

        deserialize( json, component, context );
        return component;
    }

    @Override
    public JsonElement serialize(ScoreComponent component, Type type, JsonSerializationContext context)
    {
        JsonObject root = new JsonObject();
        serialize( root, component, context );
        JsonObject json = new JsonObject();
        json.addProperty( "name", component.getName() );
        json.addProperty( "objective", component.getObjective() );
        json.addProperty( "value", component.getValue() );
        root.add( "score", json );
        return root;
    }
}
