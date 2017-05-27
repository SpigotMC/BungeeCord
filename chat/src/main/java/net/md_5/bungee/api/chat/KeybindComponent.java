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
     * The internal keybind value to use. Will be replaced with the actual key
     * the client is using.
     */
    private String keybindValue;

    /**
     * Creates a keybind component from the original to clone it.
     *
     * @param original the original for the new keybind component.
     */
    public KeybindComponent(KeybindComponent original)
    {
        super( original );
        setKeybindValue( original.getKeybindValue() );
    }

    /**
     * Creates a keybind component with the passed keybind.
     *
     * @param keybind the keybind
     */
    public KeybindComponent(Keybind keybind)
    {
        setKeybind( keybind );
    }

    /**
     * Creates a keybind component with the passed internal keybind value.
     *
     * @param keybindValue the keybind internal value
     */
    public KeybindComponent(String keybindValue)
    {
        setKeybindValue( keybindValue );
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

    /**
     * Returns the keybind for the internal keybind value or Keybind.CUSTOM if
     * there is no keybind for the internal keybind value.
     *
     * @return the keybind for the interbal keybind value
     */
    public Keybind getKeybind() {
        Keybind keybind = Keybind.getByValue( keybindValue );
        return keybind == null ? Keybind.CUSTOM : keybind;
    }

    /**
     * Sets the keykind to use.
     *
     * @param keybind the keybind
     */
    public void setKeybind(Keybind keybind) {
        keybindValue = keybind.getValue();
    }

    @Override
    protected void toPlainText(StringBuilder builder)
    {
        builder.append( getKeybind().getDefaultKey() );
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
        builder.append( getKeybind().getDefaultKey() );

        super.toLegacyText( builder );
    }

    @Override
    public String toString()
    {
        return String.format( "KeybindComponent{keybindValue=%s, %s}", keybindValue, super.toString() );
    }
}
