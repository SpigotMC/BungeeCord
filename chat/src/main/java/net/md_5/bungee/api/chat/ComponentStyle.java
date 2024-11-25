package net.md_5.bungee.api.chat;

import java.awt.Color;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

/**
 * Represents a style that may be applied to a {@link BaseComponent}.
 */
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public final class ComponentStyle implements Cloneable
{

    /**
     * The color of this style.
     * <p>
     * <b>Warning: This should be a color, not formatting code (ie,
     * {@link ChatColor#color} should not be null).</b>
     */
    private ChatColor color;
    /**
     * The shadow color of this style.
     */
    private Color shadowColor;
    /**
     * The font of this style.
     */
    private String font;
    /**
     * Whether this style is bold.
     */
    private Boolean bold;
    /**
     * Whether this style is italic.
     */
    private Boolean italic;
    /**
     * Whether this style is underlined.
     */
    private Boolean underlined;
    /**
     * Whether this style is strikethrough.
     */
    private Boolean strikethrough;
    /**
     * Whether this style is obfuscated.
     */
    private Boolean obfuscated;

    /**
     * Returns the color of this style. May return null.
     *
     * @return the color of this style, or null if default color
     */
    public ChatColor getColor()
    {
        return color;
    }

    /**
     * Returns whether or not this style has a color set.
     *
     * @return whether a color is set
     */
    public boolean hasColor()
    {
        return ( color != null );
    }

    /**
     * Returns the shadow color of this style. May return null.
     *
     * @return the shadow color of this style, or null if default color
     */
    public Color getShadowColor()
    {
        return shadowColor;
    }

    /**
     * Returns whether or not this style has a shadow color set.
     *
     * @return whether a shadow color is set
     */
    public boolean hasShadowColor()
    {
        return ( shadowColor != null );
    }

    /**
     * Returns the font of this style. May return null.
     *
     * @return the font of this style, or null if default font
     */
    public String getFont()
    {
        return font;
    }

    /**
     * Returns whether or not this style has a font set.
     *
     * @return whether a font is set
     */
    public boolean hasFont()
    {
        return ( font != null );
    }

    /**
     * Returns whether this style is bold.
     *
     * @return whether the style is bold
     */
    public boolean isBold()
    {
        return ( bold != null ) && bold.booleanValue();
    }

    /**
     * Returns whether this style is bold. May return null.
     *
     * @return whether the style is bold, or null if not set
     */
    public Boolean isBoldRaw()
    {
        return bold;
    }

    /**
     * Returns whether this style is italic. May return null.
     *
     * @return whether the style is italic
     */
    public boolean isItalic()
    {
        return ( italic != null ) && italic.booleanValue();
    }

    /**
     * Returns whether this style is italic. May return null.
     *
     * @return whether the style is italic, or null if not set
     */
    public Boolean isItalicRaw()
    {
        return italic;
    }

    /**
     * Returns whether this style is underlined.
     *
     * @return whether the style is underlined
     */
    public boolean isUnderlined()
    {
        return ( underlined != null ) && underlined.booleanValue();
    }

    /**
     * Returns whether this style is underlined. May return null.
     *
     * @return whether the style is underlined, or null if not set
     */
    public Boolean isUnderlinedRaw()
    {
        return underlined;
    }

    /**
     * Returns whether this style is strikethrough
     *
     * @return whether the style is strikethrough
     */
    public boolean isStrikethrough()
    {
        return ( strikethrough != null ) && strikethrough.booleanValue();
    }

    /**
     * Returns whether this style is strikethrough. May return null.
     *
     * @return whether the style is strikethrough, or null if not set
     */
    public Boolean isStrikethroughRaw()
    {
        return strikethrough;
    }

    /**
     * Returns whether this style is obfuscated.
     *
     * @return whether the style is obfuscated
     */
    public boolean isObfuscated()
    {
        return ( obfuscated != null ) && obfuscated.booleanValue();
    }

    /**
     * Returns whether this style is obfuscated. May return null.
     *
     * @return whether the style is obfuscated, or null if not set
     */
    public Boolean isObfuscatedRaw()
    {
        return obfuscated;
    }

    /**
     * Returns whether this style has no formatting explicitly set.
     *
     * @return true if no value is set, false if at least one is set
     */
    public boolean isEmpty()
    {
        return color == null && shadowColor == null && font == null && bold == null
                && italic == null && underlined == null
                && strikethrough == null && obfuscated == null;
    }

    @Override
    public ComponentStyle clone()
    {
        return new ComponentStyle( color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated );
    }

    /**
     * Get a new {@link ComponentStyleBuilder}.
     *
     * @return the builder
     */
    public static ComponentStyleBuilder builder()
    {
        return new ComponentStyleBuilder();
    }

    /**
     * Get a new {@link ComponentStyleBuilder} with values initialized to the
     * style values of the supplied {@link ComponentStyle}.
     *
     * @param other the component style whose values to copy into the builder
     * @return the builder
     */
    public static ComponentStyleBuilder builder(ComponentStyle other)
    {
        return new ComponentStyleBuilder()
                .color( other.color )
                .shadowColor( other.shadowColor )
                .font( other.font )
                .bold( other.bold )
                .italic( other.italic )
                .underlined( other.underlined )
                .strikethrough( other.strikethrough )
                .obfuscated( other.obfuscated );
    }
}
