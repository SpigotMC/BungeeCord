package net.md_5.bungee.api.chat;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.ChatColor;

/**
 * This component processes a target selector into a pre-formatted set of
 * discovered names.
 * <br>
 * Multiple targets may be obtained, and with commas separating each one and a
 * final "and" for the last target. The resulting format cannot be overwritten.
 * This includes all styling from team prefixes, insertions, click events, and
 * hover events.
 * <br>
 * These values are filled in by the server-side implementation.
 * <br>
 * As of 1.12.2, a bug ( MC-56373 ) prevents full usage within hover events.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class SelectorComponent extends BaseComponent
{

    /**
     * An entity target selector (@p, @a, @r, @e, or @s) and, optionally,
     * selector arguments (e.g. @e[r=10,type=Creeper]).
     */
    private String selector;

    /**
     * The separator of multiple selected entities.
     * <br>
     * The default is {@code {"color": "gray", "text": ", "}}.
     */
    private BaseComponent separator;

    /**
     * Creates a selector component from the original to clone it.
     *
     * @param original the original for the new selector component
     */
    public SelectorComponent(SelectorComponent original)
    {
        super( original );
        setSelector( original.getSelector() );
        setSeparator( original.getSeparator() );
    }

    /**
     * Creates a selector component from the selector
     *
     * @param selector the selector as a String
     */
    public SelectorComponent(String selector)
    {
        setSelector( selector );
    }

    @Override
    public SelectorComponent duplicate()
    {
        return new SelectorComponent( this );
    }

    @Override
    protected void toPlainText(StringVisitor builder)
    {
        builder.append( this.selector );
        super.toPlainText( builder );
    }

    @Override
    protected ComponentStyle toLegacyText(StringVisitor builder, ChatColor baseColor, ComponentStyle currentLegacy)
    {
        currentLegacy = addFormat( builder, baseColor, currentLegacy );
        builder.append( this.selector );
        return super.toLegacyText( builder, baseColor, currentLegacy );
    }
}
