package net.md_5.bungee.api.chat.nbt;

import lombok.Data;
import lombok.NoArgsConstructor;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.StringTag;

/**
 * Used to show entities within the {@link net.md_5.bungee.api.chat.HoverEvent} in the chat api.
 *
 * This creates dummy info and does not require any existing entity on the server for use.
 */
@Data
@NoArgsConstructor
public class NbtEntity
{

    /**
     * Name of the entity. This is optional and hidden if non present.
     */
    private String name;
    /**
     * The type of entity. Should be namespaced entity ID. Present
     * minecraft:pig if invalid.
     */
    private String type;
    /**
     * String containing the UUID of entity in a hyphenated hexadecimal format.
     * This should be a valid UUID.
     */
    private String id;

    public CompoundTag asTag()
    {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.add( "name", new StringTag( name ) );
        compoundTag.add( "type", new StringTag( type ) );
        compoundTag.add( "id", new StringTag( id ) );

        return compoundTag;
    }
}
