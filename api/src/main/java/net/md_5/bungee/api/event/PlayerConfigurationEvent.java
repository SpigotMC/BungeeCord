package net.md_5.bungee.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when the player enters Configuration mode
 */
@Data
@AllArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PlayerConfigurationEvent extends Event
{
    /**
     * The player that has entered the config mode
     */
    private final ProxiedPlayer player;
    /**
     * The status of the players configuration state
     */
    private final Status status;

    public enum Status
    {
        INITIAL_START, // used for the first configuration after login
        START,
        FINISH
    }
}
