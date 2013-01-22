package net.md_5.bungee.api.event;

import java.net.InetSocketAddress;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when the proxy is pinged with packet 0xFE from the server list.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProxyPingEvent extends Event
{

    /**
     * The address of the user pinging.
     */
    private final InetSocketAddress remoteAddress;
    /**
     * The data corresponding to the server which received this ping.
     */
    private final ListenerInfo server;
    /**
     * The data to respond with.
     */
    private ServerPing response;
}
