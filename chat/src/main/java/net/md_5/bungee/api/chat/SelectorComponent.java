package net.md_5.bungee.api.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
public final class SelectorComponent extends BaseComponent
{

    /**
     * An entity target selector (@p, @a, @r, @e, or @s) and, optionally,
     * selector arguments (e.g. @e[r=10,type=Creeper]).
     */
    private String selector;

    /**
     * Creates a selector component from the original to clone it.
     *
     * @param original the original for the new selector component
     */
    public SelectorComponent(SelectorComponent original)
    {
        super( original );
        setSelector( original.getSelector() );
    }

    @Override
    public SelectorComponent duplicate()
    {
        return new SelectorComponent( this );
    }

    @Override
    protected void toLegacyText(StringBuilder builder)
    {
        builder.append( this.selector );
        super.toLegacyText( builder );
    }
}
