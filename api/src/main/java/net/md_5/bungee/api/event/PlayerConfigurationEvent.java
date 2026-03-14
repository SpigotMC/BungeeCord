package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Called during the players configuration phase.
 *
 * For clients with version 1.20.3 or lower this event is called when the
 * proxy received the FinishConfiguration packet from the backend and
 * for newer version its called when the proxy received the KnownPack packet.
 * In both cases the packet is holded back end sent to the client when the 
 * event completes.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PlayerConfigurationEvent extends AsyncEvent<PlayerConfigurationEvent>
{

    /**
     * The player entering configuration phase.
     */
    private final ProxiedPlayer player;
    /**
     * The reason the player is entering configuration phase.
     */
    private final Reason reason;

    public PlayerConfigurationEvent(ProxiedPlayer player, Reason reason, Callback<PlayerConfigurationEvent> done)
    {
        super( done );
        this.player = player;
        this.reason = reason;
    }

    public enum Reason
    {
        /**
         * Player is switching from login phase to config phase. This only
         * happens once per connection.
         */
        LOGIN,
        /**
         * Player is switching from game phase to config phase. This can happen
         * multiple times per connection. Usually happens on server switch and
         * when the backend server reconfigures the player.
         */
        RECONFIGURE;
    }
}
