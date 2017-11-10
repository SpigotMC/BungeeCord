package net.md_5.bungee.api.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Keybind;

@Getter
@Setter
@NoArgsConstructor
public class KeybindComponent extends BaseComponent
{

    /**
     * The keybind value to use. Will be replaced with the actual key
     * the client is using.
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
     * @see {@link Keybind}
     */
    public KeybindComponent(String keybind)
    {
        setKeybind( keybind );
    }

    /**
     * Creates a duplicate of this KeybindComponent.
     *
     * @return the duplicate of this KeybindComponent.
     */
    @Override
    public BaseComponent duplicate()
    {
        return new KeybindComponent( this );
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

    @Override
    public String toString()
    {
        return String.format( "KeybindComponent{keybind=%s, %s}", keybind, super.toString() );
    }
}
