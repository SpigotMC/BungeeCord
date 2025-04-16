package net.md_5.bungee.api.chat;

import java.awt.Color;
import net.md_5.bungee.api.ChatColor;

/**
 * <p>
 * ComponentStyleBuilder simplifies creating component styles by allowing the
 * use of a chainable builder.
 * </p>
 * <pre>
 * ComponentStyle style = ComponentStyle.builder()
 *     .color(ChatColor.RED)
 *     .font("custom:font")
 *     .bold(true).italic(true).create();
 *
 * BaseComponent component = new ComponentBuilder("Hello world").style(style).create();
 * // Or it can be used directly on a component
 * TextComponent text = new TextComponent("Hello world");
 * text.applyStyle(style);
 * </pre>
 *
 * @see ComponentStyle#builder()
 * @see ComponentStyle#builder(ComponentStyle)
 */
public final class ComponentStyleBuilder
{

    private ChatColor color;
    private Color shadowColor;
    private String font;
    private Boolean bold, italic, underlined, strikethrough, obfuscated;

    /**
     * Set the style color.
     *
     * @param color the color to set, or null to use the default
     * @return this ComponentStyleBuilder for chaining
     */
    public ComponentStyleBuilder color(ChatColor color)
    {
        this.color = color;
        return this;
    }

    /**
     * Set the style shadow color.
     *
     * @param shadowColor the shadow color to set, or null to use the default
     * @return this ComponentStyleBuilder for chaining
     */
    public ComponentStyleBuilder shadowColor(Color shadowColor)
    {
        this.shadowColor = shadowColor;
        return this;
    }

    /**
     * Set the style font.
     *
     * @param font the font key to set, or null to use the default
     * @return this ComponentStyleBuilder for chaining
     */
    public ComponentStyleBuilder font(String font)
    {
        this.font = font;
        return this;
    }

    /**
     * Set the style's bold property.
     *
     * @param bold the bold value to set, or null to use the default
     * @return this ComponentStyleBuilder for chaining
     */
    public ComponentStyleBuilder bold(Boolean bold)
    {
        this.bold = bold;
        return this;
    }

    /**
     * Set the style's italic property.
     *
     * @param italic the italic value to set, or null to use the default
     * @return this ComponentStyleBuilder for chaining
     */
    public ComponentStyleBuilder italic(Boolean italic)
    {
        this.italic = italic;
        return this;
    }

    /**
     * Set the style's underlined property.
     *
     * @param underlined the underlined value to set, or null to use the default
     * @return this ComponentStyleBuilder for chaining
     */
    public ComponentStyleBuilder underlined(Boolean underlined)
    {
        this.underlined = underlined;
        return this;
    }

    /**
     * Set the style's strikethrough property.
     *
     * @param strikethrough the strikethrough value to set, or null to use the
     * default
     * @return this ComponentStyleBuilder for chaining
     */
    public ComponentStyleBuilder strikethrough(Boolean strikethrough)
    {
        this.strikethrough = strikethrough;
        return this;
    }

    /**
     * Set the style's obfuscated property.
     *
     * @param obfuscated the obfuscated value to set, or null to use the default
     * @return this ComponentStyleBuilder for chaining
     */
    public ComponentStyleBuilder obfuscated(Boolean obfuscated)
    {
        this.obfuscated = obfuscated;
        return this;
    }

    /**
     * Build the {@link ComponentStyle} using the values set in this builder.
     *
     * @return the created ComponentStyle
     */
    public ComponentStyle build()
    {
        return new ComponentStyle( color, shadowColor, font, bold, italic, underlined, strikethrough, obfuscated );
    }
}
