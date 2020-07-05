package net.md_5.bungee.api.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.ToString;

/**
 * Metadata for use in conjunction with {@link HoverEvent.Action#SHOW_ITEM}
 */
@ToString(callSuper = true)
@Getter
@Setter
@Builder(builderClassName = "Builder", access = AccessLevel.PUBLIC)
@AllArgsConstructor
@EqualsAndHashCode
public final class ItemTag
{

    private BaseComponent name;
    @Singular("ench")
    private List<Enchantment> enchantments = new ArrayList<>();
    @Singular("lore")
    private List<BaseComponent[]> lore = new ArrayList<>();
    private Boolean unbreakable;

    private ItemTag()
    {
    }

    @Getter
    @RequiredArgsConstructor
    public static class Enchantment
    {

        private final int level;
        private final int id;
    }

    public static class Serializer implements JsonSerializer<ItemTag>, JsonDeserializer<ItemTag>
    {

        @Override
        public ItemTag deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
        {
            ItemTag itemTag = new ItemTag();
            JsonObject object = element.getAsJsonObject();
            if ( object.has( "ench" ) )
            {
                for ( JsonElement jsonElement : object.get( "ench" ).getAsJsonArray() )
                {
                    JsonObject next = jsonElement.getAsJsonObject();
                    itemTag.enchantments.add( new Enchantment( next.get( "id" ).getAsInt(), next.get( "lvl" ).getAsInt() ) );
                }
            }
            if ( object.has( "Unbreakable" ) )
            {
                int status = object.get( "Unbreakable" ).getAsInt();
                if ( status == 1 )
                {
                    itemTag.unbreakable = true;
                } else if ( status == 0 )
                {
                    itemTag.unbreakable = false;
                }
            }
            if ( object.has( "display" ) )
            {
                JsonObject display = object.get( "display" ).getAsJsonObject();
                if ( display.has( "Name" ) )
                {
                    itemTag.name = context.deserialize( display.get( "Name" ).getAsJsonObject(), BaseComponent.class );
                }

                if ( display.has( "Lore" ) )
                {
                    JsonElement lore = display.get( "Lore" );
                    if ( lore.isJsonArray() )
                    {
                        for ( JsonElement loreIt : lore.getAsJsonArray() )
                        {
                            if ( loreIt.isJsonArray() )
                            {
                                itemTag.lore.add( context.deserialize( loreIt, BaseComponent[].class ) );
                            } else
                            {
                                itemTag.lore.add( new BaseComponent[]
                                {
                                    context.deserialize( loreIt, BaseComponent.class )
                                } );
                            }
                        }
                    } else
                    {
                        itemTag.lore.add( context.deserialize( display.get( "Lore" ), BaseComponent[].class ) );
                    }
                }
            }
            return itemTag;
        }

        @Override
        public JsonElement serialize(ItemTag itemTag, Type type, JsonSerializationContext context)
        {
            JsonObject object = new JsonObject();

            if ( !itemTag.enchantments.isEmpty() )
            {
                JsonArray enchArray = new JsonArray();
                for ( Enchantment ench : itemTag.enchantments )
                {
                    JsonObject enchObj = new JsonObject();
                    enchObj.addProperty( "id", ench.id );
                    enchObj.addProperty( "lvl", ench.level );
                    enchArray.add( enchObj );
                }
                object.add( "ench", enchArray );
            }

            if ( itemTag.unbreakable != null )
            {
                object.addProperty( "Unbreakable", ( itemTag.unbreakable ) ? 1 : 0 );
            }

            JsonObject display = new JsonObject();

            if ( itemTag.name != null )
            {
                display.add( "Name", context.serialize( itemTag.name ) );
            }

            if ( !itemTag.lore.isEmpty() )
            {
                display.add( "Lore", context.serialize( itemTag.lore ) );
            }

            if ( display.size() != 0 )
            {
                object.add( "display", display );
            }

            return object;
        }
    }
}
