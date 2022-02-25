package net.md_5.bungee.api.event;

import java.net.SocketAddress;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when a connection attempt was denied by the connection throttle.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ConnectionThrottledEvent extends Event
{

    /**
     * Remote address of connection.
     */
    private final SocketAddress socketAddress;
}
