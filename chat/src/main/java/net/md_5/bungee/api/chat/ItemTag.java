package net.md_5.bungee.api.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
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
@Builder(builderClassName = "Builder", access = AccessLevel.PRIVATE)
@ToString(of = "nbt")
@EqualsAndHashCode(of = "nbt")
@Setter
@AllArgsConstructor
public final class ItemTag
{

    @Getter
    private final String nbt;

    private BaseComponent name;
    @Singular("ench")
    private List<Enchantment> enchantments;
    @Singular("lore")
    private List<BaseComponent[]> lore;
    private Boolean unbreakable;

    private ItemTag(String nbt)
    {
        this.nbt = nbt;
    }

    public static ItemTag ofNbt(String nbt)
    {
        return new ItemTag( nbt );
    }

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
            // Remove the enclosing string quotes.
            String eString = element.toString();
            if ( eString.length() >= 2 && eString.charAt( 0 ) == '\"' && eString.charAt( eString.length() - 1 ) == '\"' )
            {
                eString = eString.substring( 1, eString.length() - 1 );
            }

            return ItemTag.ofNbt( eString );
        }

        @Override
        public JsonElement serialize(ItemTag itemTag, Type type, JsonSerializationContext context)
        {
            return context.serialize( itemTag.getNbt() );
        }
    }
}
