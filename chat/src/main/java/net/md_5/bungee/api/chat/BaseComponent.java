package net.md_5.bungee.api.chat;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;

@Setter
@ToString(exclude = "parent")
@EqualsAndHashCode(exclude = "parent")
public abstract class BaseComponent
{

    @Setter(AccessLevel.NONE)
    BaseComponent parent;

    /**
     * The color of this component and any child components (unless overridden)
     */
    private ChatColor color;
    /**
     * The font of this component and any child components (unless overridden)
     */
    private String font;
    /**
     * Whether this component and any child components (unless overridden) is
     * bold
     */
    private Boolean bold;
    /**
     * Whether this component and any child components (unless overridden) is
     * italic
     */
    private Boolean italic;
    /**
     * Whether this component and any child components (unless overridden) is
     * underlined
     */
    private Boolean underlined;
    /**
     * Whether this component and any child components (unless overridden) is
     * strikethrough
     */
    private Boolean strikethrough;
    /**
     * Whether this component and any child components (unless overridden) is
     * obfuscated
     */
    private Boolean obfuscated;
    /**
     * The text to insert into the chat when this component (and child
     * components) are clicked while pressing the shift key
     */
    @Getter
    private String insertion;

    /**
     * Appended components that inherit this component's formatting and events
     */
    @Getter
    private List<BaseComponent> extra;

    /**
     * The action to perform when this component (and child components) are
     * clicked
     */
    @Getter
    private ClickEvent clickEvent;
    /**
     * The action to perform when this component (and child components) are
     * hovered over
     */
    @Getter
    private HoverEvent hoverEvent;

    /**
     * Whether this component rejects previous formatting
     */
    @Getter
    private transient boolean reset;

    /**
     * Default constructor.
     *
     * @deprecated for use by internal classes only, will be removed.
     */
    @Deprecated
    public BaseComponent()
    {
    }

    BaseComponent(BaseComponent old)
    {
        copyFormatting( old, FormatRetention.ALL, true );

        if ( old.getExtra() != null )
        {
            for ( BaseComponent extra : old.getExtra() )
            {
                addExtra( extra.duplicate() );
            }
        }
    }

    /**
     * Copies the events and formatting of a BaseComponent. Already set
     * formatting will be replaced.
     *
     * @param component the component to copy from
     */
    public void copyFormatting(BaseComponent component)
    {
        copyFormatting( component, FormatRetention.ALL, true );
    }

    /**
     * Copies the events and formatting of a BaseComponent.
     *
     * @param component the component to copy from
     * @param replace if already set formatting should be replaced by the new
     * component
     */
    public void copyFormatting(BaseComponent component, boolean replace)
    {
        copyFormatting( component, FormatRetention.ALL, replace );
    }

    /**
     * Copies the specified formatting of a BaseComponent.
     *
     * @param component the component to copy from
     * @param retention the formatting to copy
     * @param replace if already set formatting should be replaced by the new
     * component
     */
    public void copyFormatting(BaseComponent component, FormatRetention retention, boolean replace)
    {
        if ( retention == FormatRetention.EVENTS || retention == FormatRetention.ALL )
        {
            if ( replace || clickEvent == null )
            {
                setClickEvent( component.getClickEvent() );
            }
            if ( replace || hoverEvent == null )
            {
                setHoverEvent( component.getHoverEvent() );
            }
        }
        if ( retention == FormatRetention.FORMATTING || retention == FormatRetention.ALL )
        {
            if ( replace || color == null )
            {
                setColor( component.getColorRaw() );
            }
            if ( replace || font == null )
            {
                setFont( component.getFontRaw() );
            }
            if ( replace || bold == null )
            {
                setBold( component.isBoldRaw() );
            }
            if ( replace || italic == null )
            {
                setItalic( component.isItalicRaw() );
            }
            if ( replace || underlined == null )
            {
                setUnderlined( component.isUnderlinedRaw() );
            }
            if ( replace || strikethrough == null )
            {
                setStrikethrough( component.isStrikethroughRaw() );
            }
            if ( replace || obfuscated == null )
            {
                setObfuscated( component.isObfuscatedRaw() );
            }
            if ( replace || insertion == null )
            {
                setInsertion( component.getInsertion() );
            }
        }
    }

    /**
     * Retains only the specified formatting.
     *
     * @param retention the formatting to retain
     */
    public void retain(FormatRetention retention)
    {
        if ( retention == FormatRetention.FORMATTING || retention == FormatRetention.NONE )
        {
            setClickEvent( null );
            setHoverEvent( null );
        }
        if ( retention == FormatRetention.EVENTS || retention == FormatRetention.NONE )
        {
            setColor( null );
            setBold( null );
            setItalic( null );
            setUnderlined( null );
            setStrikethrough( null );
            setObfuscated( null );
            setInsertion( null );
        }
    }

    /**
     * Clones the BaseComponent and returns the clone.
     *
     * @return The duplicate of this BaseComponent
     */
    public abstract BaseComponent duplicate();

    /**
     * Clones the BaseComponent without formatting and returns the clone.
     *
     * @return The duplicate of this BaseComponent
     * @deprecated API use discouraged, use traditional duplicate
     */
    @Deprecated
    public BaseComponent duplicateWithoutFormatting()
    {
        BaseComponent component = duplicate();
        component.retain( FormatRetention.NONE );
        return component;
    }

    /**
     * Converts the components to a string that uses the old formatting codes
     * ({@link net.md_5.bungee.api.ChatColor#COLOR_CHAR}
     *
     * @param components the components to convert
     * @return the string in the old format
     */
    public static String toLegacyText(BaseComponent... components)
    {
        StringBuilder builder = new StringBuilder();
        for ( BaseComponent msg : components )
        {
            builder.append( msg.toLegacyText() );
        }
        return builder.toString();
    }

    /**
     * Converts the components into a string without any formatting
     *
     * @param components the components to convert
     * @return the string as plain text
     */
    public static String toPlainText(BaseComponent... components)
    {
        StringBuilder builder = new StringBuilder();
        for ( BaseComponent msg : components )
        {
            builder.append( msg.toPlainText() );
        }
        return builder.toString();
    }

    /**
     * Returns the color of this component. This uses the parent's color if this
     * component doesn't have one. {@link net.md_5.bungee.api.ChatColor#WHITE}
     * is returned if no color is found.
     *
     * @return the color of this component
     */
    public ChatColor getColor()
    {
        if ( color == null )
        {
            if ( parent == null )
            {
                return ChatColor.WHITE;
            }
            return parent.getColor();
        }
        return color;
    }

    /**
     * Returns the color of this component without checking the parents color.
     * May return null
     *
     * @return the color of this component
     */
    public ChatColor getColorRaw()
    {
        return color;
    }

    /**
     * Returns the font of this component. This uses the parent's font if this
     * component doesn't have one.
     *
     * @return the font of this component, or null if default font
     */
    public String getFont()
    {
        if ( font == null )
        {
            if ( parent == null )
            {
                return null;
            }
            return parent.getFont();
        }
        return font;
    }

    /**
     * Returns the font of this component without checking the parents font. May
     * return null
     *
     * @return the font of this component
     */
    public String getFontRaw()
    {
        return font;
    }

    /**
     * Returns whether this component is bold. This uses the parent's setting if
     * this component hasn't been set. false is returned if none of the parent
     * chain has been set.
     *
     * @return whether the component is bold
     */
    public boolean isBold()
    {
        if ( bold == null )
        {
            return parent != null && parent.isBold();
        }
        return bold;
    }

    /**
     * Returns whether this component is bold without checking the parents
     * setting. May return null
     *
     * @return whether the component is bold
     */
    public Boolean isBoldRaw()
    {
        return bold;
    }

    /**
     * Returns whether this component is italic. This uses the parent's setting
     * if this component hasn't been set. false is returned if none of the
     * parent chain has been set.
     *
     * @return whether the component is italic
     */
    public boolean isItalic()
    {
        if ( italic == null )
        {
            return parent != null && parent.isItalic();
        }
        return italic;
    }

    /**
     * Returns whether this component is italic without checking the parents
     * setting. May return null
     *
     * @return whether the component is italic
     */
    public Boolean isItalicRaw()
    {
        return italic;
    }

    /**
     * Returns whether this component is underlined. This uses the parent's
     * setting if this component hasn't been set. false is returned if none of
     * the parent chain has been set.
     *
     * @return whether the component is underlined
     */
    public boolean isUnderlined()
    {
        if ( underlined == null )
        {
            return parent != null && parent.isUnderlined();
        }
        return underlined;
    }

    /**
     * Returns whether this component is underlined without checking the parents
     * setting. May return null
     *
     * @return whether the component is underlined
     */
    public Boolean isUnderlinedRaw()
    {
        return underlined;
    }

    /**
     * Returns whether this component is strikethrough. This uses the parent's
     * setting if this component hasn't been set. false is returned if none of
     * the parent chain has been set.
     *
     * @return whether the component is strikethrough
     */
    public boolean isStrikethrough()
    {
        if ( strikethrough == null )
        {
            return parent != null && parent.isStrikethrough();
        }
        return strikethrough;
    }

    /**
     * Returns whether this component is strikethrough without checking the
     * parents setting. May return null
     *
     * @return whether the component is strikethrough
     */
    public Boolean isStrikethroughRaw()
    {
        return strikethrough;
    }

    /**
     * Returns whether this component is obfuscated. This uses the parent's
     * setting if this component hasn't been set. false is returned if none of
     * the parent chain has been set.
     *
     * @return whether the component is obfuscated
     */
    public boolean isObfuscated()
    {
        if ( obfuscated == null )
        {
            return parent != null && parent.isObfuscated();
        }
        return obfuscated;
    }

    /**
     * Returns whether this component is obfuscated without checking the parents
     * setting. May return null
     *
     * @return whether the component is obfuscated
     */
    public Boolean isObfuscatedRaw()
    {
        return obfuscated;
    }

    public void setExtra(List<BaseComponent> components)
    {
        for ( BaseComponent component : components )
        {
            component.parent = this;
        }
        extra = components;
    }

    /**
     * Appends a text element to the component. The text will inherit this
     * component's formatting
     *
     * @param text the text to append
     */
    public void addExtra(String text)
    {
        addExtra( new TextComponent( text ) );
    }

    /**
     * Appends a component to the component. The text will inherit this
     * component's formatting
     *
     * @param component the component to append
     */
    public void addExtra(BaseComponent component)
    {
        if ( extra == null )
        {
            extra = new ArrayList<BaseComponent>();
        }
        component.parent = this;
        extra.add( component );
    }

    /**
     * Returns whether the component has any formatting or events applied to it
     *
     * @return Whether any formatting or events are applied
     */
    public boolean hasFormatting()
    {
        return color != null || font != null || bold != null
                || italic != null || underlined != null
                || strikethrough != null || obfuscated != null
                || insertion != null || hoverEvent != null || clickEvent != null;
    }

    /**
     * Converts the component into a string without any formatting
     *
     * @return the string as plain text
     */
    public String toPlainText()
    {
        StringBuilder builder = new StringBuilder();
        toPlainText( builder );
        return builder.toString();
    }

    void toPlainText(StringBuilder builder)
    {
        if ( extra != null )
        {
            for ( BaseComponent e : extra )
            {
                e.toPlainText( builder );
            }
        }
    }

    /**
     * Converts the component to a string that uses the old formatting codes
     * ({@link net.md_5.bungee.api.ChatColor#COLOR_CHAR}
     *
     * @return the string in the old format
     */
    public String toLegacyText()
    {
        StringBuilder builder = new StringBuilder();
        toLegacyText( builder );
        return builder.toString();
    }

    void toLegacyText(StringBuilder builder)
    {
        if ( extra != null )
        {
            for ( BaseComponent e : extra )
            {
                e.toLegacyText( builder );
            }
        }
    }

    void addFormat(StringBuilder builder)
    {
        builder.append( getColor() );
        if ( isBold() )
        {
            builder.append( ChatColor.BOLD );
        }
        if ( isItalic() )
        {
            builder.append( ChatColor.ITALIC );
        }
        if ( isUnderlined() )
        {
            builder.append( ChatColor.UNDERLINE );
        }
        if ( isStrikethrough() )
        {
            builder.append( ChatColor.STRIKETHROUGH );
        }
        if ( isObfuscated() )
        {
            builder.append( ChatColor.MAGIC );
        }
    }
}
