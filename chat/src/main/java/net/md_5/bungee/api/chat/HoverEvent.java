package net.md_5.bungee.api.chat;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
final public class HoverEvent
{

    private final Action action;
    private final BaseComponent[] value;

    public enum Action
    {

        SHOW_TEXT,
        SHOW_ACHIEVEMENT,
        SHOW_ITEM,
        SHOW_ENTITY
    }

    /**
     * Creates a HoverEvent with the passed action and value
     *
     * @param action the action
     * @param value the value
     */
    @java.beans.ConstructorProperties({"action", "value"})
    public HoverEvent(Action action, BaseComponent[] value)
    {
        this.action = action;
        this.value = value;
    }

    /**
     * Creates a HoverEvent with the passed action and value
     *
     * @param action the action
     * @param value the value
     */
    @java.beans.ConstructorProperties({"action", "value"})
    public HoverEvent(Action action, BaseComponent value)
    {
        this( action, new BaseComponent[] { value } );
    }
}
