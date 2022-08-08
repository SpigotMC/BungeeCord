package net.md_5.bungee.api.chat;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.ChatColor;

/**
 * <p>
 * ComponentBuilder simplifies creating basic messages by allowing the use of a
 * chainable builder.
 * </p>
 * <pre>
 * new ComponentBuilder("Hello ").color(ChatColor.RED).
 * append("World").color(ChatColor.BLUE). append("!").bold(true).create();
 * </pre>
 * <p>
 * All methods (excluding {@link #append(String)} and {@link #create()} work on
 * the last part appended to the builder, so in the example above "Hello " would
 * be {@link net.md_5.bungee.api.ChatColor#RED} and "World" would be
 * {@link net.md_5.bungee.api.ChatColor#BLUE} but "!" would be bold and
 * {@link net.md_5.bungee.api.ChatColor#BLUE} because append copies the previous
 * part's formatting
 * </p>
 */
@NoArgsConstructor
public final class ComponentBuilder
{

    /**
     * The position for the current part to modify. Modified cursors will
     * automatically reset to the last part after appending new components.
     * Default value at -1 to assert that the builder has no parts.
     */
    @Getter
    private int cursor = -1;
    @Getter
    private final List<BaseComponent> parts = new ArrayList<BaseComponent>();
    private BaseComponent dummy;

    private ComponentBuilder(BaseComponent[] parts)
    {
        for ( BaseComponent baseComponent : parts )
        {
            this.parts.add( baseComponent.duplicate() );
        }
        resetCursor();
    }

    /**
     * Creates a ComponentBuilder from the other given ComponentBuilder to clone
     * it.
     *
     * @param original the original for the new ComponentBuilder.
     */
    public ComponentBuilder(ComponentBuilder original)
    {
        this( original.parts.toArray( new BaseComponent[ 0 ] ) );
    }

    /**
     * Creates a ComponentBuilder with the given text as the first part.
     *
     * @param text the first text element
     */
    public ComponentBuilder(String text)
    {
        this( new TextComponent( text ) );
    }

    /**
     * Creates a ComponentBuilder with the given component as the first part.
     *
     * @param component the first component element
     */
    public ComponentBuilder(BaseComponent component)
    {

        this( new BaseComponent[]
        {
            component
        } );
    }

    private BaseComponent getDummy()
    {
        if ( dummy == null )
        {
            dummy = new BaseComponent()
            {
                @Override
                public BaseComponent duplicate()
                {
                    return this;
                }
            };
        }
        return dummy;
    }

    /**
     * Resets the cursor to index of the last element.
     *
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder resetCursor()
    {
        cursor = parts.size() - 1;
        return this;
    }

    /**
     * Sets the position of the current component to be modified
     *
     * @param pos the cursor position synonymous to an element position for a
     * list
     * @return this ComponentBuilder for chaining
     * @throws IndexOutOfBoundsException if the index is out of range
     * ({@code index < 0 || index >= size()})
     */
    public ComponentBuilder setCursor(int pos) throws IndexOutOfBoundsException
    {
        if ( ( this.cursor != pos ) && ( pos < 0 || pos >= parts.size() ) )
        {
            throw new IndexOutOfBoundsException( "Cursor out of bounds (expected between 0 + " + ( parts.size() - 1 ) + ")" );
        }

        this.cursor = pos;
        return this;
    }

    /**
     * Appends a component to the builder and makes it the current target for
     * formatting. The component will have all the formatting from previous
     * part.
     *
     * @param component the component to append
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder append(BaseComponent component)
    {
        return append( component, FormatRetention.ALL );
    }

    /**
     * Appends a component to the builder and makes it the current target for
     * formatting. You can specify the amount of formatting retained from
     * previous part.
     *
     * @param component the component to append
     * @param retention the formatting to retain
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder append(BaseComponent component, FormatRetention retention)
    {
        BaseComponent previous = ( parts.isEmpty() ) ? null : parts.get( parts.size() - 1 );
        if ( previous == null )
        {
            previous = dummy;
            dummy = null;
        }
        if ( previous != null && !component.isReset() )
        {
            component.copyFormatting( previous, retention, false );
        }
        parts.add( component );
        resetCursor();
        return this;
    }

    /**
     * Appends the components to the builder and makes the last element the
     * current target for formatting. The components will have all the
     * formatting from previous part.
     *
     * @param components the components to append
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder append(BaseComponent[] components)
    {
        return append( components, FormatRetention.ALL );
    }

    /**
     * Appends the components to the builder and makes the last element the
     * current target for formatting. You can specify the amount of formatting
     * retained from previous part.
     *
     * @param components the components to append
     * @param retention the formatting to retain
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder append(BaseComponent[] components, FormatRetention retention)
    {
        Preconditions.checkArgument( components.length != 0, "No components to append" );

        for ( BaseComponent component : components )
        {
            append( component, retention );
        }

        return this;
    }

    /**
     * Appends the text to the builder and makes it the current target for
     * formatting. The text will have all the formatting from previous part.
     *
     * @param text the text to append
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder append(String text)
    {
        return append( text, FormatRetention.ALL );
    }

    /**
     * Parse text to BaseComponent[] with colors and format, appends the text to
     * the builder and makes it the current target for formatting. The component
     * will have all the formatting from previous part.
     *
     * @param text the text to append
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder appendLegacy(String text)
    {
        return append( TextComponent.fromLegacyText( text ) );
    }

    /**
     * Appends the text to the builder and makes it the current target for
     * formatting. You can specify the amount of formatting retained from
     * previous part.
     *
     * @param text the text to append
     * @param retention the formatting to retain
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder append(String text, FormatRetention retention)
    {
        return append( new TextComponent( text ), retention );
    }

    /**
     * Allows joining additional components to this builder using the given
     * {@link Joiner} and {@link FormatRetention#ALL}.
     *
     * Simply executes the provided joiner on this instance to facilitate a
     * chain pattern.
     *
     * @param joiner joiner used for operation
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder append(Joiner joiner)
    {
        return joiner.join( this, FormatRetention.ALL );
    }

    /**
     * Allows joining additional components to this builder using the given
     * {@link Joiner}.
     *
     * Simply executes the provided joiner on this instance to facilitate a
     * chain pattern.
     *
     * @param joiner joiner used for operation
     * @param retention the formatting to retain
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder append(Joiner joiner, FormatRetention retention)
    {
        return joiner.join( this, retention );
    }

    /**
     * Remove the component part at the position of given index.
     *
     * @param pos the index to remove at
     * @throws IndexOutOfBoundsException if the index is out of range
     * ({@code index < 0 || index >= size()})
     */
    public void removeComponent(int pos) throws IndexOutOfBoundsException
    {
        if ( parts.remove( pos ) != null )
        {
            resetCursor();
        }
    }

    /**
     * Gets the component part at the position of given index.
     *
     * @param pos the index to find
     * @return the component
     * @throws IndexOutOfBoundsException if the index is out of range
     * ({@code index < 0 || index >= size()})
     */
    public BaseComponent getComponent(int pos) throws IndexOutOfBoundsException
    {
        return parts.get( pos );
    }

    /**
     * Gets the component at the position of the cursor.
     *
     * @return the active component or null if builder is empty
     */
    public BaseComponent getCurrentComponent()
    {
        return ( cursor == -1 ) ? getDummy() : parts.get( cursor );
    }

    /**
     * Sets the color of the current part.
     *
     * @param color the new color
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder color(ChatColor color)
    {
        getCurrentComponent().setColor( color );
        return this;
    }

    /**
     * Sets the font of the current part.
     *
     * @param font the new font
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder font(String font)
    {
        getCurrentComponent().setFont( font );
        return this;
    }

    /**
     * Sets whether the current part is bold.
     *
     * @param bold whether this part is bold
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder bold(boolean bold)
    {
        getCurrentComponent().setBold( bold );
        return this;
    }

    /**
     * Sets whether the current part is italic.
     *
     * @param italic whether this part is italic
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder italic(boolean italic)
    {
        getCurrentComponent().setItalic( italic );
        return this;
    }

    /**
     * Sets whether the current part is underlined.
     *
     * @param underlined whether this part is underlined
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder underlined(boolean underlined)
    {
        getCurrentComponent().setUnderlined( underlined );
        return this;
    }

    /**
     * Sets whether the current part is strikethrough.
     *
     * @param strikethrough whether this part is strikethrough
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder strikethrough(boolean strikethrough)
    {
        getCurrentComponent().setStrikethrough( strikethrough );
        return this;
    }

    /**
     * Sets whether the current part is obfuscated.
     *
     * @param obfuscated whether this part is obfuscated
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder obfuscated(boolean obfuscated)
    {
        getCurrentComponent().setObfuscated( obfuscated );
        return this;
    }

    /**
     * Sets the insertion text for the current part.
     *
     * @param insertion the insertion text
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder insertion(String insertion)
    {
        getCurrentComponent().setInsertion( insertion );
        return this;
    }

    /**
     * Sets the click event for the current part.
     *
     * @param clickEvent the click event
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder event(ClickEvent clickEvent)
    {
        getCurrentComponent().setClickEvent( clickEvent );
        return this;
    }

    /**
     * Sets the hover event for the current part.
     *
     * @param hoverEvent the hover event
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder event(HoverEvent hoverEvent)
    {
        getCurrentComponent().setHoverEvent( hoverEvent );
        return this;
    }

    /**
     * Sets the current part back to normal settings. Only text is kept.
     *
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder reset()
    {
        return retain( FormatRetention.NONE );
    }

    /**
     * Retains only the specified formatting. Text is not modified.
     *
     * @param retention the formatting to retain
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder retain(FormatRetention retention)
    {
        getCurrentComponent().retain( retention );
        return this;
    }

    /**
     * Returns the components needed to display the message created by this
     * builder.git
     *
     * @return the created components
     */
    public BaseComponent[] create()
    {
        BaseComponent[] cloned = new BaseComponent[ parts.size() ];
        int i = 0;
        for ( BaseComponent part : parts )
        {
            cloned[i++] = part.duplicate();
        }
        return cloned;
    }

    public enum FormatRetention
    {

        /**
         * Specify that we do not want to retain anything from the previous
         * component.
         */
        NONE,
        /**
         * Specify that we want the formatting retained from the previous
         * component.
         */
        FORMATTING,
        /**
         * Specify that we want the events retained from the previous component.
         */
        EVENTS,
        /**
         * Specify that we want to retain everything from the previous
         * component.
         */
        ALL
    }

    /**
     * Functional interface to join additional components to a ComponentBuilder.
     */
    public interface Joiner
    {

        /**
         * Joins additional components to the provided {@link ComponentBuilder}
         * and then returns it to fulfill a chain pattern.
         *
         * Retention may be ignored and is to be understood as an optional
         * recommendation to the Joiner and not as a guarantee to have a
         * previous component in builder unmodified.
         *
         * @param componentBuilder to which to append additional components
         * @param retention the formatting to possibly retain
         * @return input componentBuilder for chaining
         */
        ComponentBuilder join(ComponentBuilder componentBuilder, FormatRetention retention);
    }
}
