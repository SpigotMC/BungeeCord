package net.md_5.bungee.api.chat.nbt;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Data
@RequiredArgsConstructor
public class NbtItem
{

    /**
     * Namespaced item ID
     */
    private final String id;
    private Tag tag;

    @ToString
    @Data
    @RequiredArgsConstructor
    public static class Tag
    {

        protected Display display;

        @Data
        @ToString
        public static class Display
        {

            protected List<String> lore;
        }
    }

    public void setLore(List<String> lore)
    {
        if ( tag == null ) tag = new Tag();
        if ( tag.display == null ) tag.display = new Tag.Display();
        tag.display.lore = lore;
    }

    public static class Serializer implements JsonSerializer<NbtItem>, JsonDeserializer<NbtItem>
    {

        @Override
        public NbtItem deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject json = element.getAsJsonObject();
            String id = json.get( "id" ).getAsString();

            NbtItem item = new NbtItem( id );

            if ( json.has( "tag" ) )
            {
                JsonObject tag = json.get( "tag" ).getAsJsonObject();
                if ( tag.has( "display" ) )
                {
                    JsonObject display = tag.get( "display" ).getAsJsonObject();
                    if ( display.has( "lore" ) )
                    {
                        item.setLore( Arrays.asList( context.<String[]>deserialize( display.get( "lore" ), String[].class ) ) );
                    }
                }
            }

            return item;
        }

        @Override
        public JsonElement serialize(NbtItem item, Type type, JsonSerializationContext context)
        {
            JsonObject root = new JsonObject();
            root.addProperty( "id", item.getId() );
            root.addProperty( "count", 1 ); // No use-case for different value?

            if ( item.tag != null )
            {
                JsonObject tag = new JsonObject();
                if ( item.tag.display != null )
                {
                    JsonObject display = new JsonObject();
                    if ( item.tag.display.lore != null )
                    {
                        display.add( "lore", context.serialize( item.tag.display.lore ) );
                    }
                    tag.add( "display", display );
                }
                root.add( "tag", tag );
            }
            return root;
        }
    }
}
