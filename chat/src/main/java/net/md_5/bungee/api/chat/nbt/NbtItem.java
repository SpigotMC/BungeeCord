package net.md_5.bungee.api.chat.nbt;

import com.google.common.base.Preconditions;
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

/**
 * Used to show items within the {@link net.md_5.bungee.api.chat.HoverEvent} in the chat api.
 *
 * This creates dummy info and does not require any existing item on the server for use.
 */
@Data
@RequiredArgsConstructor
public class NbtItem
{

    /**
     * Namespaced item ID.
     */
    private final String id;
    /**
     * Tag data for this item.
     */
    private Tag tag;

    @Data
    @RequiredArgsConstructor
    public static class Tag
    {

        /**
         * Display information for this item.
         */
        protected Display display;

        @Data
        @ToString
        public static class Display
        {

            protected List<String> lore;
        }
    }

    /**
     * Sets the lore of the item.
     *
     * @param lore the lore to set
     */
    public void setLore(List<String> lore)
    {
        Preconditions.checkNotNull( lore, "lore" );
        if ( tag == null ) tag = new Tag();
        if ( tag.display == null ) tag.display = new Tag.Display();
        tag.display.lore = lore;
    }

    /**
     * Serialises this item into a JSON format recognised by the Minecraft chat.
     */
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
