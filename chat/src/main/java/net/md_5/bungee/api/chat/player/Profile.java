package net.md_5.bungee.api.chat.player;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class Profile
{

    /**
     * The name of the profile. Can be null.
     */
    private String name;
    /**
     * The UUID of the profile. Can be null.
     */
    private UUID uuid;
    /**
     * The properties of the profile. Can be null.
     */
    private Property[] properties;

    public Profile(@NonNull String name)
    {
        this( name, null, null );
    }

    public Profile(@NonNull UUID uuid)
    {
        this( null, uuid, null );
    }

    public Profile(@NonNull Property[] properties)
    {
        this( null, null, properties );
    }
}
