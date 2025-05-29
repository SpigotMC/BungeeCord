package net.md_5.bungee.dialog;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.md_5.bungee.api.dialog.action.StaticAction;
import net.md_5.bungee.chat.ClickEventSerializer;

public class ChatClickEventWrapperSerializer implements JsonDeserializer<StaticAction.ChatClickEventWrapper>, JsonSerializer<StaticAction.ChatClickEventWrapper>
{

    @Override
    public StaticAction.ChatClickEventWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return new StaticAction.ChatClickEventWrapper( ClickEventSerializer.DIALOG.deserialize( json.getAsJsonObject(), context ) );
    }

    @Override
    public JsonElement serialize(StaticAction.ChatClickEventWrapper src, Type typeOfSrc, JsonSerializationContext context)
    {
        return ClickEventSerializer.DIALOG.serialize( src.event(), context );
    }
}
