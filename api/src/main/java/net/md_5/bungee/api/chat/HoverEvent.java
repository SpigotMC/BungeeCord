package net.md_5.bungee.api.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class HoverEvent
{
    @Getter
    @Setter
    private Action action;

    @Getter
    private Object value;

    public HoverEvent(Action action, String value)
    {
        setAction( action );
        setValue( value );
    }

    public HoverEvent(Action action, BaseComponent value)
    {
        setAction( action );
        setValue( value );
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public void setValue(BaseComponent value)
    {
        this.value = value;
    }

    public enum Action
    {
        SHOW_TEXT,
        SHOW_ACHIEVEMENT,
        SHOW_ITEM
    }
}
