package net.md_5.bungee.api.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * This component processes a target selector into a pre-formatted set of discovered names.<br>
 * Multiple targets may be obtained, and with commas seperating each one and a final "and" for the last target.
 * The resulting format cannot be overwritten. This includes all styling from team prefixes, insertions, click events, and hover events.<br>
 * A bug (MC-53673) currently prevents full usage within hover events.
 */
@Getter
@ToString
@RequiredArgsConstructor
public final class SelectorComponent extends BaseComponent
{
    /**
     * An entity target selector (@p, @a, @r, @e, or @s) and, optionally, selector arguments (e.g. @e[r=10,type=Creeper]).
     */
    private final String selector;

    /**
     * Creates a selector component from the original to clone it.
     *
     * @param original the original for the new selector component
     */
    public SelectorComponent(SelectorComponent original)
    {
        super(original);
        this.selector = original.getSelector();
    }

    @Override
    public SelectorComponent duplicate()
    {
        return new SelectorComponent(this);
    }

    @Override
    public SelectorComponent duplicateWithoutFormatting()
    {
        return new SelectorComponent(this.selector);
    }

    protected void toLegacyText(StringBuilder builder)
    {
        builder.append(this.selector);
    }
}
