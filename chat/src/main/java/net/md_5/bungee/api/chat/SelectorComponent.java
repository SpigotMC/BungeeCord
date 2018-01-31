package net.md_5.bungee.api.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public final class SelectorComponent extends BaseComponent
{
    /**
     * A string containing a selector (@p, @a, @r, @e, or @s) and, optionally, selector arguments.
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
