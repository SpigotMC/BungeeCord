package net.md_5.bungee.api.chat;

import lombok.NoArgsConstructor;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * ComponentBuilder simplifies creating basic messages by allowing
 * the use of a chainable builder.
 * <p/>
 * <pre>
 *     new ComponentBuilder("Hello ").color(ChatColor.RED).
 *         append("World").color(ChatColor.BLUE).
 *         append("!").bold(true).create();
 * </pre>
 * <p/>
 * All methods (excluding {@link #append(String)} and {@link #create()}
 * work on the last part appended to the builder, so in the example
 * above "Hello " would be {@link net.md_5.bungee.api.ChatColor#RED}
 * and "World" would be {@link net.md_5.bungee.api.ChatColor#BLUE} but
 * "!" would be bold and {@link net.md_5.bungee.api.ChatColor#BLUE}
 * because append copies the previous part's formatting
 */
public class ComponentBuilder
{
    private TextComponent current;
    private List<BaseComponent> parts = new ArrayList<>();

    /**
     * Creates a componentBuilder with the given text as the
     * first part.
     *
     * @param text the first text element
     */
    public ComponentBuilder(String text)
    {
        current = new TextComponent( text );
    }

    /**
     * Appends the text to the builder and makes it the current
     * target for formatting. The text will have all the
     * formatting from the previous part.
     *
     * @param text the text to append
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder append(String text)
    {
        parts.add( current );
        current = new TextComponent( current );
        current.setText( text );
        return this;
    }

    /**
     * Sets the color of the current part.
     *
     * @param color the new color
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder color(ChatColor color)
    {
        current.setColor( color );
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
        current.setBold( bold );
        return this;
    }

    /**
     * Sets whether the current part is italic
     *
     * @param italic whether this part is italic
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder italic(boolean italic)
    {
        current.setItalic( italic );
        return this;
    }

    /**
     * Sets whether the current part is underlined
     *
     * @param underlined whether this part is underlined
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder underlined(boolean underlined)
    {
        current.setUnderlined( underlined );
        return this;
    }

    /**
     * Sets whether the current part is strikethrough
     *
     * @param strikethrough whether this part is strikethrough
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder strikethrough(boolean strikethrough)
    {
        current.setStrikethrough( strikethrough );
        return this;
    }

    /**
     * Sets whether the current pat is obfuscated
     *
     * @param obfuscated whether this part is obfuscated
     * @return this ComponentBuilder for chaining
     */
    public ComponentBuilder obfuscated(boolean obfuscated)
    {
        current.setObfuscated( obfuscated );
        return this;
    }

    /**
     * Sets the click event for the current part.
     * @param clickEvent
     * @return
     */
    public ComponentBuilder event(ClickEvent clickEvent)
    {
        current.setClickEvent( clickEvent );
        return this;
    }

    public ComponentBuilder event(HoverEvent hoverEvent)
    {
        current.setHoverEvent( hoverEvent );
        return this;
    }

    /**
     * Returns the components needed to display the message
     * created by this builder
     * @return the created components
     */
    public BaseComponent[] create()
    {
        parts.add( current );
        return parts.toArray( new BaseComponent[parts.size()] );
    }
}
