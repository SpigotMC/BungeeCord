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
    /**
     * The reason the player is entering configuration phase.
     */
    private Reason reason;

    public PlayerConfigurationEvent(ProxiedPlayer player, Reason reason, Callback<PlayerConfigurationEvent> done)
    {
        super( done );
        this.player = player;
        this.reason = reason;
    }

    public enum Reason
    {
        /**
         * Player is switching from login phase to config phase.
         * This only happens once per connection.
         */
        LOGIN,

        /**
         * Player is switching from game phase to config phase.
         * This can happen multiple times per connection.
         * For example, when switching servers.
         * Or if the backend itself reconfigures the player.
         */
        RECONFIGURE
    }
}
