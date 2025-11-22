package net.md_5.bungee.api.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.chat.objects.ChatObject;

/**
 * An object component that can be used to display objects.
 * <p>
 * It can either display a player's head or an object by a specific sprite and
 * an atlas.
 * <p>
 * Note: this was added in Minecraft 1.21.9.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public final class ObjectComponent extends BaseComponent
{

    private ChatObject object;

    /**
     * Creates a ObjectComponent from a given ChatObject.
     *
     * See {@link net.md_5.bungee.api.chat.objects.PlayerObject} and
     * {@link net.md_5.bungee.api.chat.objects.SpriteObject}.
     *
     * @param object the ChatObject
     */
    public ObjectComponent(@NonNull ChatObject object)
    {
        this.object = object;
    }

    /**
     * Creates an object component from the original to clone it.
     *
     * @param original the original for the new score component
     */
    public ObjectComponent(ObjectComponent original)
    {
        super( original );
        setObject( original.object );
    }

    @Override
    public ObjectComponent duplicate()
    {
        return new ObjectComponent( this );
    }

    @Override
    protected void toPlainText(StringVisitor builder)
    {
        // I guess we cannot convert this to plain text
        // builder.append( this.value );
        super.toPlainText( builder );
    }

    @Override
    protected void toLegacyText(StringVisitor builder)
    {
        addFormat( builder );
        // Same here...
        // builder.append( this.value );
        super.toLegacyText( builder );
    }
}
