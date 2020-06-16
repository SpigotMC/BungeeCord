package net.md_5.bungee.api.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.chat.nbt.NbtEntity;
import net.md_5.bungee.api.chat.nbt.NbtItem;

@Getter
@ToString
@EqualsAndHashCode
public final class HoverEvent
{

    private final Action action;
    private final Object value;

    public HoverEvent(Action action, BaseComponent[] value)
    {
        this( action, (Object) value );
    }

    public static HoverEvent showItem(NbtItem nbt)
    {
        return new HoverEvent( Action.SHOW_ITEM, nbt );
    }

    public static HoverEvent showEntity(NbtEntity nbt)
    {
        return new HoverEvent( Action.SHOW_ENTITY, nbt );
    }

    private HoverEvent(Action action, Object value)
    {
        this.action = action;
        this.value = value;
    }

    public enum Action
    {

        SHOW_TEXT,
        SHOW_ACHIEVEMENT,
        SHOW_ITEM,
        SHOW_ENTITY
    }
}
