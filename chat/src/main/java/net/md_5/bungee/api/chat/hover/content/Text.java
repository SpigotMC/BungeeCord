package net.md_5.bungee.api.chat.hover.content;

import java.util.Arrays;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;

@Getter
@ToString
public class Text extends Content
{

    /**
     * The value.
     *
     * May be a component or raw text depending on constructor used.
     */
    private final Object value;

    public Text(BaseComponent[] value)
    {
        this.value = value;
    }

    public Text(String value)
    {
        this.value = value;
    }

    @Override
    public HoverEvent.Action requiredAction()
    {
        return HoverEvent.Action.SHOW_TEXT;
    }

    @Override
    public boolean equals(Object o)
    {
        if ( value instanceof BaseComponent[] )
        {
            return o instanceof Text
                    && ( (Text) o ).value instanceof BaseComponent[]
                    && Arrays.equals( (BaseComponent[]) value, (BaseComponent[]) ( (Text) o ).value );
        } else
        {
            return value.equals( o );
        }
    }

    @Override
    public int hashCode()
    {
        return ( value instanceof BaseComponent[] ) ? Arrays.hashCode( (BaseComponent[]) value ) : value.hashCode();
    }
}
