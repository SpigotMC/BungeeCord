package net.md_5.bungee.api.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
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
     * @see Keybinds
     */
    public KeybindComponent(String keybind)
    {
        setKeybind( keybind );
    }

    @Override
    public KeybindComponent duplicate()
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
        addFormat( builder );
        builder.append( getKeybind() );
        super.toLegacyText( builder );
    }

    @Override
    String toStringName()
    {
        return "Keybind";
    }

    @Override
    boolean toString(StringBuilder builder, boolean comma)
    {
        if ( comma ) builder.append( ", " );
        comma = true;
        builder.append( "keybind=" ).append( keybind );
        return super.toString( builder, comma );
    }

}
