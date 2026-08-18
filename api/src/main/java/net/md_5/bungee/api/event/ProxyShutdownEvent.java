package net.md_5.bungee.api.event;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.plugin.Event;

/**
 * This event is fired when the proxy is shutting down before players are disconnected
 */
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ProxyShutdownEvent extends Event
{

}
