package net.md_5.bungee.api.event;

import java.net.SocketAddress;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Event called to represent an initial client connection.
 * <br>
 * Note: This event is called at an early stage of every connection, handling
 * should be <b>fast</b>.
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
     * Remote address of connection.
     */
    @NotNull
    private final SocketAddress socketAddress;
    /**
     * Listener that accepted the connection.
     */
    @NotNull
    private final ListenerInfo listener;
}
