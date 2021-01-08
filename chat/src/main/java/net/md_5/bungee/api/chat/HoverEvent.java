package net.md_5.bungee.api.chat;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class HoverEvent
{

    /**
     * The action of this event.
     */
    private final Action action;
    /**
     * List of contents to provide for this event.
     */
    private final List<Content> contents;
    /**
     * Returns whether this hover event is prior to 1.16
     */
    @Setter
    private boolean legacy = false;

    /**
     * Creates event with an action and a list of contents.
     *
     * @param action action of this event
     * @param contents array of contents, provide at least one
     */
    public HoverEvent(Action action, Content... contents)
    {
        Preconditions.checkArgument( contents.length != 0,
                "Must contain at least one content" );
        this.action = action;
        this.contents = new ArrayList<>();
        for ( Content it : contents )
        {
            addContent( it );
        }
    }

    /**
     * Legacy constructor to create hover event.
     *
     * @param action the action
     * @param value the value
     * @deprecated {@link #HoverEvent(Action, Content[])}
     */
    @Deprecated
    public HoverEvent(Action action, BaseComponent[] value)
    {
        // Old plugins may have somehow hacked BaseComponent[] into
        // anything other than SHOW_TEXT action. Ideally continue support.
        this.action = action;
        this.contents = new ArrayList<>( Collections.singletonList( new Text( value ) ) );
        this.legacy = true;
    }

    @Deprecated
    public BaseComponent[] getValue()
    {
        Content content = contents.get( 0 );
        if ( content instanceof Text && ( (Text) content ).getValue() instanceof BaseComponent[] )
        {
            return (BaseComponent[]) ( (Text) content ).getValue();
        }

        TextComponent component = new TextComponent( ComponentSerializer.toString( content ) );
        return new BaseComponent[]
        {
            component
        };
    }

    /**
     * Adds a content to this hover event.
     *
     * @param content the content add
     * @throws IllegalArgumentException if is a legacy component and already has
     * a content
     * @throws UnsupportedOperationException if content action does not match
     * hover event action
     */
    public void addContent(Content content) throws UnsupportedOperationException
    {
        Preconditions.checkArgument( !legacy || contents.size() == 0,
                "Legacy HoverEvent may not have more than one content" );
        content.assertAction( action );
        contents.add( content );
    }

    public enum Action
    {

        SHOW_TEXT,
        SHOW_ITEM,
        SHOW_ENTITY,
        /**
         * Removed since 1.12. Advancements instead simply use show_text. The ID
         * of an achievement or statistic to display. Example: new
         * ComponentText( "achievement.openInventory" )
         */
        @Deprecated
        SHOW_ACHIEVEMENT,
    }

    /**
     * Gets the appropriate {@link Content} class for an {@link Action} for the
     * GSON serialization
     *
     * @param action the action to get for
     * @param array if to return the arrayed class
     * @return the class
     */
    public static Class<?> getClass(HoverEvent.Action action, boolean array)
    {
        Preconditions.checkArgument( action != null, "action" );

        switch ( action )
        {
            case SHOW_TEXT:
                return ( array ) ? Text[].class : Text.class;
            case SHOW_ENTITY:
                return ( array ) ? Entity[].class : Entity.class;
            case SHOW_ITEM:
                return ( array ) ? Item[].class : Item.class;
            default:
                throw new UnsupportedOperationException( "Action '" + action.name() + " not supported" );
        }
    }
}
