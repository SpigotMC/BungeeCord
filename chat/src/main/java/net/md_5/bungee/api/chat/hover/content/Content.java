package net.md_5.bungee.api.chat.hover.content;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.chat.HoverEvent;

@ToString
@EqualsAndHashCode
public abstract class Content
{

    /**
     * Required action for this content type.
     *
     * @return action
     */
    public abstract HoverEvent.Action requiredAction();

    /**
     * Tests this content against an action
     *
     * @param input input to test
     * @throws UnsupportedOperationException if action incompatible
     */
    public void assertAction(HoverEvent.Action input) throws UnsupportedOperationException
    {
        if ( input != requiredAction() )
        {
            throw new UnsupportedOperationException( "Action " + input + " not compatible! Expected " + requiredAction() );
        }
    }
}
