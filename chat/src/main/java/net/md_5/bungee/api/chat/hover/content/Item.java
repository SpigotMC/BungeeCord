package net.md_5.bungee.api.chat.hover.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;

@Data
@AllArgsConstructor
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
     * Optional. Item tag.
     */
    private ItemTag tag;

    @Override
    public HoverEvent.Action requiredAction()
    {
        return HoverEvent.Action.SHOW_ITEM;
    }
}
