package net.md_5.bungee.api.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.ChatColor;

@Getter
@Setter
@ToString
@NoArgsConstructor
public final class KeybindComponent extends BaseComponent
{

    /**
     * The keybind identifier to use.
     * <br>
     * Will be replaced with the actual key the client is using.
     */
    private String keybind;

    /**
     * Creates a keybind component from the original to clone it.
     *
     * @param original the original for the new keybind component.
     */
    public KeybindComponent(KeybindComponent original)
    {
        super( original );
        setKeybind( original.getKeybind() );
    }

    /**
     * Creates a keybind component with the passed internal keybind value.
     *
     * @param keybind the keybind value
     * @see Keybind
     */
    public KeybindComponent(String keybind)
    {
        setKeybind( keybind );
    }

    @Override
    public BaseComponent duplicate()
    {
        return new KeybindComponent( this );
    }

    @Override
    public BaseComponent duplicateWithoutFormatting()
    {
        return new KeybindComponent( keybind );
    }

    @Override
    protected void toPlainText(StringBuilder builder)
    {
        builder.append( getKeybind() );
        super.toPlainText( builder );
    }

    @Override
    protected void toLegacyText(StringBuilder builder)
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
        builder.append( getKeybind() );

        super.toLegacyText( builder );
    }
}
