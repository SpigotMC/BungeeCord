package net.md_5.bungee.api.chat.hover.content;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;

@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class Item extends Content
{

    /**
     * Namespaced item ID. Will use 'minecraft:air' if null.
     */
    private String id;
    /**
     * Optional. Size of the item stack.
     */
    private int count = -1;
    /**
     * Optional. Item components.
     */
    private JsonElement components;
    /**
     * @deprecated
     * Optional. Item tag.
     */
    @Deprecated
    private ItemTag tag;

    /**
     * @deprecated Since 1.20.5+, you need to use the new constructors for the Item Components system instead.
     */
    @Deprecated
    public Item(String id, int count, ItemTag tag)
    {
        this.id = id;
        this.count = count;
        this.tag = tag;
    }

    /**
     * Note: You need to use this constructor with versions 1.20.5 and higher.
     * Example code:
     * <pre>
     * new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item(Bukkit.getUnsafe().serializeItemAsJson(ItemStack item)))
     * </pre>
     * @param itemJson The item to create the hover event for.
     */
    public Item(JsonObject itemJson) 
    {
        JsonElement itemId = itemJson.get("id");
        JsonElement itemCount = itemJson.get("count");

        this.id = itemId != null ? itemId.getAsString() : null;
        this.count = itemCount != null ? itemCount.getAsInt() : -1;
        this.components = itemJson.get("components");
    }

    /**
     * Note: You can only use this constructor with versions 1.20.5 and higher.
     * @param id The item id.
     * @param count The item count.
     * @param components The item components.
     */
    public Item(String id, int count, JsonElement components) 
    {
        this.id = id;
        this.count = count;
        this.components = components;
    }

    @Override
    public HoverEvent.Action requiredAction()
    {
        return HoverEvent.Action.SHOW_ITEM;
    }
}
