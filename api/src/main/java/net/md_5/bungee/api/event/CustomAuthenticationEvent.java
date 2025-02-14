package net.md_5.bungee.api.event;

import java.util.UUID;
import lombok.Data;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.protocol.Property;

/**
 * This Event is called if the mojang authentication is disabled,
 * and we need to authenticate the player ourselves.
 */
@Data
public class CustomAuthenticationEvent extends AsyncEvent<CustomAuthenticationEvent>
{
    /*
     * The pending connection that needs to be authenticated
     */
    private final PendingConnection connection;
    /*
    * The name of the players session
    */
    private String name;
    /*
     * The uuid of the players session
     */
    private UUID uuid;
    /*
     * The properties of the players session (skin, cape, etc.)
     */
    private Property[] properties;

    public CustomAuthenticationEvent(PendingConnection connection, Callback<CustomAuthenticationEvent> done)
    {
        super( done );
        this.connection = connection;
    }

}
