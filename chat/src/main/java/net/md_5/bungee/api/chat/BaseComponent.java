package net.md_5.bungee.api.chat;

import java.awt.Color;
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
     * The component's style.
     */
    @Getter
    private ComponentStyle style = new ComponentStyle();
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
            if ( replace || !style.hasColor() )
            {
                setColor( component.getColorRaw() );
            }
            if ( replace || !style.hasShadowColor() )
            {
                setShadowColor( component.getShadowColorRaw() );
            }
            if ( replace || !style.hasFont() )
            {
                setFont( component.getFontRaw() );
            }
            if ( replace || style.isBoldRaw() == null )
            {
                setBold( component.isBoldRaw() );
            }
            if ( replace || style.isItalicRaw() == null )
            {
                setItalic( component.isItalicRaw() );
            }
            if ( replace || style.isUnderlinedRaw() == null )
            {
                setUnderlined( component.isUnderlinedRaw() );
            }
            if ( replace || style.isStrikethroughRaw() == null )
            {
                setStrikethrough( component.isStrikethroughRaw() );
            }
            if ( replace || style.isObfuscatedRaw() == null )
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
            setShadowColor( null );
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
     * Set the {@link ComponentStyle} for this component.
     * <p>
     * Unlike {@link #applyStyle(ComponentStyle)}, this method will overwrite
     * all style values on this component.
     *
     * @param style the style to set, or null to set all style values to default
     */
    public void setStyle(ComponentStyle style)
    {
        this.style = ( style != null ) ? style.clone() : new ComponentStyle();
    }

    /**
     * Set this component's color.
     * <p>
     * <b>Warning: This should be a color, not formatting code (ie,
     * {@link ChatColor#color} should not be null).</b>
     *
     * @param color the component color, or null to use the default
     */
    public void setColor(ChatColor color)
    {
        this.style.setColor( color );
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
        if ( !style.hasColor() )
        {
            if ( parent == null )
            {
                return ChatColor.WHITE;
            }
            return parent.getColor();
        }
        return style.getColor();
    }

    /**
     * Returns the color of this component without checking the parents color.
     * May return null
     *
     * @return the color of this component
     */
    public ChatColor getColorRaw()
    {
        return style.getColor();
    }

    /**
     * Set this component's shadow color.
     *
     * @param color the component shadow color, or null to use the default
     */
    public void setShadowColor(Color color)
    {
        this.style.setShadowColor( color );
    }

    /**
     * Returns the shadow color of this component. This uses the parent's shadow color if this
     * component doesn't have one. null is returned if no shadow color is found.
     *
     * @return the shadow color of this component
     */
    public Color getShadowColor()
    {
        if ( !style.hasShadowColor() )
        {
            if ( parent == null )
            {
                return null;
            }
            return parent.getShadowColor();
        }
        return style.getShadowColor();
    }

    /**
     * Returns the shadow color of this component without checking the parents
     * shadow color. May return null
     *
     * @return the shadow color of this component
     */
    public Color getShadowColorRaw()
    {
        return style.getShadowColor();
    }

    /**
     * Set this component's font.
     *
     * @param font the font to set, or null to use the default
     */
    public void setFont(String font)
    {
        this.style.setFont( font );
    }

    /**
     * Returns the font of this component. This uses the parent's font if this
     * component doesn't have one.
     *
     * @return the font of this component, or null if default font
     */
    public String getFont()
    {
        if ( !style.hasFont() )
        {
            if ( parent == null )
            {
                return null;
            }
            return parent.getFont();
        }
        return style.getFont();
    }

    /**
     * Returns the font of this component without checking the parents font. May
     * return null
     *
     * @return the font of this component
     */
    public String getFontRaw()
    {
        return style.getFont();
    }

    /**
     * Set whether or not this component is bold.
     *
     * @param bold the new bold state, or null to use the default
     */
    public void setBold(Boolean bold)
    {
        this.style.setBold( bold );
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
        if ( style.isBoldRaw() == null )
        {
            return parent != null && parent.isBold();
        }
        return style.isBold();
    }

    /**
     * Returns whether this component is bold without checking the parents
     * setting. May return null
     *
     * @return whether the component is bold
     */
    public Boolean isBoldRaw()
    {
        return style.isBoldRaw();
    }

    /**
     * Set whether or not this component is italic.
     *
     * @param italic the new italic state, or null to use the default
     */
    public void setItalic(Boolean italic)
    {
        this.style.setItalic( italic );
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
        if ( style.isItalicRaw() == null )
        {
            return parent != null && parent.isItalic();
        }
        return style.isItalic();
    }

    /**
     * Returns whether this component is italic without checking the parents
     * setting. May return null
     *
     * @return whether the component is italic
     */
    public Boolean isItalicRaw()
    {
        return style.isItalicRaw();
    }

    /**
     * Set whether or not this component is underlined.
     *
     * @param underlined the new underlined state, or null to use the default
     */
    public void setUnderlined(Boolean underlined)
    {
        this.style.setUnderlined( underlined );
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
        if ( style.isUnderlinedRaw() == null )
        {
            return parent != null && parent.isUnderlined();
        }
        return style.isUnderlined();
    }

    /**
     * Returns whether this component is underlined without checking the parents
     * setting. May return null
     *
     * @return whether the component is underlined
     */
    public Boolean isUnderlinedRaw()
    {
        return style.isUnderlinedRaw();
    }

    /**
     * Set whether or not this component is strikethrough.
     *
     * @param strikethrough the new strikethrough state, or null to use the
     * default
     */
    public void setStrikethrough(Boolean strikethrough)
    {
        this.style.setStrikethrough( strikethrough );
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
        if ( style.isStrikethroughRaw() == null )
        {
            return parent != null && parent.isStrikethrough();
        }
        return style.isStrikethrough();
    }

    /**
     * Returns whether this component is strikethrough without checking the
     * parents setting. May return null
     *
     * @return whether the component is strikethrough
     */
    public Boolean isStrikethroughRaw()
    {
        return style.isStrikethroughRaw();
    }

    /**
     * Set whether or not this component is obfuscated.
     *
     * @param obfuscated the new obfuscated state, or null to use the default
     */
    public void setObfuscated(Boolean obfuscated)
    {
        this.style.setObfuscated( obfuscated );
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
        if ( style.isObfuscatedRaw() == null )
        {
            return parent != null && parent.isObfuscated();
        }
        return style.isObfuscated();
    }

    /**
     * Returns whether this component is obfuscated without checking the parents
     * setting. May return null
     *
     * @return whether the component is obfuscated
     */
    public Boolean isObfuscatedRaw()
    {
        return style.isObfuscatedRaw();
    }

    /**
     * Apply the style from the given {@link ComponentStyle} to this component.
     * <p>
     * Any style values that have been explicitly set in the style will be
     * applied to this component. If a value is not set in the style, it will
     * not override the style set in this component.
     *
     * @param style the style to apply
     */
    public void applyStyle(ComponentStyle style)
    {
        if ( style.hasColor() )
        {
            setColor( style.getColor() );
        }
        if ( style.hasShadowColor() )
        {
            setShadowColor( style.getShadowColor() );
        }
        if ( style.hasFont() )
        {
            setFont( style.getFont() );
        }
        if ( style.isBoldRaw() != null )
        {
            setBold( style.isBoldRaw() );
        }
        if ( style.isItalicRaw() != null )
        {
            setItalic( style.isItalicRaw() );
        }
        if ( style.isUnderlinedRaw() != null )
        {
            setUnderlined( style.isUnderlinedRaw() );
        }
        if ( style.isStrikethroughRaw() != null )
        {
            setStrikethrough( style.isStrikethroughRaw() );
        }
        if ( style.isObfuscatedRaw() != null )
        {
            setObfuscated( style.isObfuscatedRaw() );
        }
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
     * Returns whether the component has any styling applied to it.
     *
     * @return Whether any styling is applied
     */
    public boolean hasStyle()
    {
        return !style.isEmpty();
    }

    /**
     * Returns whether the component has any formatting or events applied to it
     *
     * @return Whether any formatting or events are applied
     */
    public boolean hasFormatting()
    {
        return hasStyle() || insertion != null
                || hoverEvent != null || clickEvent != null;
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
