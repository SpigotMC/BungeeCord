package net.md_5.bungee.api.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.ApiStatus;

/**
 * An object component that can be used to display objects.
 * <p>
 * It can either display a players head or
 * an object by a specific sprite and an atlas
 * Note: this was added in Minecraft 1.21.9
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public final class ObjectComponent extends BaseComponent
{

    /**
     * The name of the players head to be displayed.
     */
    private String player;

    /**
     * The namespaced ID of a sprite atlas, default value: minecraft:blocks
     */
    private String atlas;

    /**
     * The namespaced ID of a sprite in atlas, for example item/porkchop
     */
    private String sprite;

    /**
     * Creates a player head object component.
     * @param player the name of the player
     */
    public ObjectComponent(@NonNull String player)
    {
        setPlayer( player );
    }

    /**
     * Creates a sprite object component.
     * @param atlas the namespaced ID of a sprite atlas, default value: minecraft:blocks
     * @param sprite the namespaced ID of a sprite in atlas, for example item/porkchop
     */
    public ObjectComponent(String atlas, @NonNull String sprite)
    {
        setAtlas( atlas );
        setSprite( sprite );
    }

    @ApiStatus.Internal
    public ObjectComponent(String player, String atlas, String sprite)
    {
        setPlayer( player );
        setAtlas( atlas );
        setSprite( sprite );
    }

    /**
     * Creates a score component from the original to clone it.
     *
     * @param original the original for the new score component
     */
    public ObjectComponent(ObjectComponent original)
    {
        super( original );
        setPlayer( original.getPlayer() );
        setAtlas( original.getAtlas() );
        setSprite( original.getSprite() );
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
