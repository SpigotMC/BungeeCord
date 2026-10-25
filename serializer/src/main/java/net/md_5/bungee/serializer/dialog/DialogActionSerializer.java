package net.md_5.bungee.serializer.dialog;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.dialog.action.Action;
import net.md_5.bungee.api.dialog.action.CustomClickAction;
import net.md_5.bungee.api.dialog.action.RunCommandAction;
import net.md_5.bungee.api.dialog.action.StaticAction;
import net.md_5.bungee.chat.ClickEventSerializer;

public class DialogActionSerializer implements JsonDeserializer<Action>, JsonSerializer<Action>
{

    private static final BiMap<String, Class<? extends Action>> DYNAMIC;

    static
    {
        ImmutableBiMap.Builder<String, Class<? extends Action>> builder = ImmutableBiMap.builder();

        builder.put( "minecraft:dynamic/custom", CustomClickAction.class );
        builder.put( "minecraft:dynamic/run_command", RunCommandAction.class );

        DYNAMIC = builder.build();
    }

    @Override
    public Action deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = json.getAsJsonObject();
        String type = object.get( "type" ).getAsString();

        Class<? extends Action> realType = DYNAMIC.get( type );
        if ( realType == null )
        {
            ClickEvent click = ClickEventSerializer.DIALOG.deserialize( json.getAsJsonObject(), context );
            return new StaticAction( click );
        } else
        {
            return context.deserialize( json, realType );
        }
    }

    @Override
    public JsonElement serialize(Action src, Type typeOfSrc, JsonSerializationContext context)
    {
        if ( src == null )
        {
            return JsonNull.INSTANCE;
        }

        if ( src instanceof StaticAction )
        {
            return ClickEventSerializer.DIALOG.serialize( ( (StaticAction) src ).clickEvent(), context );
        } else
        {
            Class<? extends Action> realType = src.getClass();
            String type = DYNAMIC.inverse().get( realType );
            Preconditions.checkArgument( type != null, "Unknown type %s", typeOfSrc );

            JsonObject object = (JsonObject) context.serialize( src, realType );
            object.addProperty( "type", type );

            return object;
        }
    }
}
