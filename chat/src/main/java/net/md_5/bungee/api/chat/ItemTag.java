package net.md_5.bungee.api.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Metadata for use in conjunction with {@link HoverEvent.Action#SHOW_ITEM}
 */
@Builder(builderClassName = "Builder", access = AccessLevel.PRIVATE)
@ToString(of = "nbt")
@EqualsAndHashCode(of = "nbt")
@Setter
public final class ItemTag
{

    @Getter
    private final String nbt;

    /*
    TODO
    private BaseComponent name;
    @Singular("ench")
    private List<Enchantment> enchantments;
    @Singular("lore")
    private List<BaseComponent[]> lore;
    private Boolean unbreakable;

    @RequiredArgsConstructor
    public static class Enchantment
    {

        private final int level;
        private final int id;
    }
    */

    private ItemTag(String nbt)
    {
        this.nbt = nbt;
    }

    public static ItemTag ofNbt(String nbt)
    {
        return new ItemTag( nbt );
    }

    public static class Serializer implements JsonSerializer<ItemTag>, JsonDeserializer<ItemTag>
    {

        @Override
        public ItemTag deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
        {
            return ItemTag.ofNbt( element.getAsJsonPrimitive().getAsString() );
        }

        @Override
        public JsonElement serialize(ItemTag itemTag, Type type, JsonSerializationContext context)
        {
            return context.serialize( itemTag.getNbt() );
        }
    }
}
