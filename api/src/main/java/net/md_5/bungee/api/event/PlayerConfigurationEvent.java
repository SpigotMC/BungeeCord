package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Called when the player enters configuration phase.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PlayerConfigurationEvent extends AsyncEvent<PlayerConfigurationEvent>
{
    /**
     * The player entering configuration phase.
     */
    private ProxiedPlayer player;

    public PlayerConfigurationEvent(ProxiedPlayer player, Callback<PlayerConfigurationEvent> done)
    {
        super( done );
        this.player = player;
    }
}
