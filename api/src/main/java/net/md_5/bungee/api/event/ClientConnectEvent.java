package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event called to represent initialization of connection.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ClientConnectEvent extends Event implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Remote Adderss of user.
     */
    private final ListenerInfo listenerInfo;

    public ClientConnectEvent(ListenerInfo listenerInfo)
    {
        this.listenerInfo = listenerInfo;
    }
}
