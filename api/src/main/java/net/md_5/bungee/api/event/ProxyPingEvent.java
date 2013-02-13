package net.md_5.bungee.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when the proxy is pinged with packet 0xFE from the server list.
 */
@Data
@AllArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ProxyPingEvent extends Event
{

    /**
     * The connection asking for a ping response.
     */
    private final PendingConnection connection;
    /**
     * The data to respond with.
     */
    private ServerPing response;
}
