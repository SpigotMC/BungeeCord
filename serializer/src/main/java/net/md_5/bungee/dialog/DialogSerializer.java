package net.md_5.bungee.dialog;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.dialog.ConfirmationDialog;
import net.md_5.bungee.api.dialog.Dialog;
import net.md_5.bungee.api.dialog.DialogBase;
import net.md_5.bungee.api.dialog.DialogListDialog;
import net.md_5.bungee.api.dialog.MultiActionDialog;
import net.md_5.bungee.api.dialog.MultiActionInputFormDialog;
import net.md_5.bungee.api.dialog.NoticeDialog;
import net.md_5.bungee.api.dialog.ServerLinksDialog;
import net.md_5.bungee.api.dialog.SimpleInputFormDialog;
import net.md_5.bungee.chat.VersionedComponentSerializer;

@RequiredArgsConstructor
public class DialogSerializer implements JsonDeserializer<Dialog>, JsonSerializer<Dialog>
{

    private static final BiMap<String, Class<? extends Dialog>> TYPES;
    private final VersionedComponentSerializer serializer;

    static
    {
        ImmutableBiMap.Builder<String, Class<? extends Dialog>> builder = ImmutableBiMap.builder();

        builder.put( "minecraft:notice", NoticeDialog.class );
        builder.put( "minecraft:confirmation", ConfirmationDialog.class );
        builder.put( "minecraft:multi_action", MultiActionDialog.class );
        builder.put( "minecraft:server_links", ServerLinksDialog.class );
        builder.put( "minecraft:dialog_list", DialogListDialog.class );
        builder.put( "minecraft:simple_input_form", SimpleInputFormDialog.class );
        builder.put( "minecraft:multi_action_input_form", MultiActionInputFormDialog.class );

        TYPES = builder.build();
    }

    public JsonElement toJson(Dialog dialog)
    {
        return serializer.getGson().toJsonTree( dialog, Dialog.class );
    }

    public String toString(Dialog dialog)
    {
        return serializer.getGson().toJson( dialog, Dialog.class );
    }

    public Dialog deserialize(JsonElement jsonElement)
    {
        return serializer.getGson().fromJson( jsonElement, Dialog.class );
    }

    public Dialog deserialize(String json)
    {
        JsonElement jsonElement = JsonParser.parseString( json );
        return deserialize( jsonElement );
    }

    @Override
    public Dialog deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = json.getAsJsonObject();

        String type = object.get( "type" ).getAsString();

        if ( object.has( "base" ) )
        {
            throw new JsonParseException( "Cannot explicitly specify base" );
        }

        Type realType = TYPES.get( type );
        if ( realType == null )
        {
            throw new JsonParseException( "Unknown type " + type );
        }

        Dialog dialog = context.deserialize( json, realType );

        DialogBase base = context.deserialize( json, DialogBase.class );
        dialog.setBase( base );

        return dialog;
    }

    @Override
    public JsonElement serialize(Dialog src, Type typeOfSrc, JsonSerializationContext context)
    {
        if ( src == null )
        {
            return JsonNull.INSTANCE;
        }

        Class<? extends Dialog> realType = src.getClass();
        String type = TYPES.inverse().get( realType );
        Preconditions.checkArgument( type != null, "Unknown type %s", typeOfSrc );

        JsonObject object = (JsonObject) context.serialize( src, realType );
        object.addProperty( "type", type );

        JsonObject base = (JsonObject) context.serialize( src.getBase() );
        object.asMap().putAll( base.asMap() );

        return object;
    }
}
