package net.md_5.bungee.api.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class HoverEvent
{
    private Action action;
    private BaseComponent value;

    public enum Action
    {
        SHOW_TEXT,
        SHOW_ACHIEVEMENT,
        SHOW_ITEM
    }

    @Override
    public String toString() {
        return String.format( "HoverEvent{action=%s, value=%s}", action, value );
    }
}
