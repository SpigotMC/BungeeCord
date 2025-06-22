package net.md_5.bungee.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Arrays;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class TranslatableComponentSerializer extends BaseComponentSerializer implements JsonSerializer<TranslatableComponent>, JsonDeserializer<TranslatableComponent>
{

    public TranslatableComponentSerializer(VersionedComponentSerializer serializer)
    {
        super( serializer );
    }

    @Override
    public TranslatableComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        TranslatableComponent component = new TranslatableComponent();
        JsonObject object = json.getAsJsonObject();
        deserialize( object, component, context );
        JsonElement translate = object.get( "translate" );
        if ( translate == null )
        {
            throw new JsonParseException( "Could not parse JSON: missing 'translate' property" );
        }
        component.setTranslate( translate.getAsString() );
        JsonElement with = object.get( "with" );
        if ( with != null )
        {
            component.setWith( Arrays.asList( context.deserialize( with, BaseComponent[].class ) ) );
        }
        JsonElement fallback = object.get( "fallback" );
        if ( fallback != null )
        {
            component.setFallback( fallback.getAsString() );
        }
        return component;
    }

    @Override
    public JsonElement serialize(TranslatableComponent src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject object = new JsonObject();
        serialize( object, src, context );
        object.addProperty( "translate", src.getTranslate() );
        if ( src.getWith() != null )
        {
            object.add( "with", context.serialize( src.getWith() ) );
        }
        if ( src.getFallback() != null )
        {
            object.addProperty( "fallback", src.getFallback() );
        }
        return object;
    }
}
