package net.md_5.bungee.api.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Keybind;

import java.util.ArrayList;
import java.util.Arrays;

@Getter
@Setter
@AllArgsConstructor
public class KeybindComponent extends BaseComponent
{

    /**
     * The keybind name of the component
     */
    private Keybind keybind;

    /**
     * Creates a KeybindComponent without a keybind.
     */
    public KeybindComponent()
    {
        this.keybind = Keybind.NONE;
    }

    /**
     * Creates a KeybindComponent with the formatting and keybind from the passed
     * component
     *
     * @param keybindComponent the component to copy from
     */
    public KeybindComponent( KeybindComponent keybindComponent )
    {
        super( keybindComponent );
        setKeybind( keybindComponent.getKeybind() );
    }

    /**
     * Creates a KeybindComponent with the given keybind and the extras set to the passed
     * array
     *
     * @param keybind the keybind to display
     * @param extras  the extras to set
     */
    public KeybindComponent( Keybind keybind, BaseComponent... extras )
    {
        setKeybind( keybind );
        setExtra( new ArrayList<BaseComponent>( Arrays.asList( extras ) ) );
    }

    /**
     * Creates a duplicate of this KeybindComponent.
     *
     * @return the duplicate of this KeybindComponent.
     */
    @Override
    public BaseComponent duplicate( )
    {
        return new KeybindComponent( this );
    }

    @Override
    public BaseComponent duplicateWithoutFormatting( )
    {
        return new KeybindComponent( this.keybind );
    }

    @Override
    protected void toPlainText( StringBuilder builder )
    {
        builder.append( this.keybind );
        super.toPlainText( builder );
    }

    @Override
    protected void toLegacyText( StringBuilder builder )
    {
        builder.append( getColor() );
        if ( isBold() ) {
            builder.append( ChatColor.BOLD );
        }
        if ( isItalic() ) {
            builder.append( ChatColor.ITALIC );
        }
        if ( isUnderlined() ) {
            builder.append( ChatColor.UNDERLINE );
        }
        if ( isStrikethrough() ) {
            builder.append( ChatColor.STRIKETHROUGH );
        }
        if ( isObfuscated() ) {
            builder.append( ChatColor.MAGIC );
        }
        builder.append( this.keybind );
        super.toLegacyText( builder );
    }

    @Override
    public String toString( )
    {
        return String.format( "KeybindComponent{keybind=%s, %s}", keybind, super.toString() );
    }
}
