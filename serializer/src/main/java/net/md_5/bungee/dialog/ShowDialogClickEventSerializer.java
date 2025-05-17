package net.md_5.bungee.dialog;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.md_5.bungee.api.dialog.Dialog;
import net.md_5.bungee.api.dialog.chat.ShowDialogClickEvent;

public class ShowDialogClickEventSerializer implements JsonDeserializer<ShowDialogClickEvent>, JsonSerializer<ShowDialogClickEvent>
{

    @Override
    public ShowDialogClickEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if ( json.isJsonPrimitive() && json.getAsJsonPrimitive().isString() )
        {
            return new ShowDialogClickEvent( json.getAsJsonPrimitive().getAsString() );
        }

        return new ShowDialogClickEvent( (Dialog) context.deserialize( json, Dialog.class ) );
    }

    @Override
    public JsonElement serialize(ShowDialogClickEvent src, Type typeOfSrc, JsonSerializationContext context)
    {
        if ( src.getReference() != null )
        {
            return new JsonPrimitive( src.getReference() );
        }

        return context.serialize( src.getDialog(), Dialog.class );
    }
}
