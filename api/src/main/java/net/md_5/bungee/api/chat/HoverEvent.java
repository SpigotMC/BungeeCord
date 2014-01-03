package net.md_5.bungee.api.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
final public class HoverEvent
{

    private final Action action;
    private final BaseComponent[] value;

    public enum Action
    {

        SHOW_TEXT,
        SHOW_ACHIEVEMENT,
        SHOW_ITEM
    }

    @Override
    public String toString()
    {
        return String.format( "HoverEvent{action=%s, value=%s}", action, value );
    }
}
