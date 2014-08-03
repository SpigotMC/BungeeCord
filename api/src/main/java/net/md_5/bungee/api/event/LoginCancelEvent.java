package net.md_5.bungee.api.event;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Event;

/**
 * Fired when a pending connection is disconnected before PostLoginEvent has been called (before the ProxiedPlayer
 * instance has been created) and thus PlayerDisconnectEvent will not be sent.
 *
 * @author yawkat
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class LoginCancelEvent extends Event
{
    private final PendingConnection connection;
}
