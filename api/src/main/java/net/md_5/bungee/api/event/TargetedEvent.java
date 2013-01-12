package net.md_5.bungee.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.md_5.bungee.api.connection.Connection;

/**
 * An event which occurs in the communication between two nodes. It is not
 * recommended to call {@link #setSender(net.md_5.bungee.api.Connection)} or
 * {@link #setReceiver(net.md_5.bungee.api.Connection)} and the results of doing
 * so are undefined.
 */
@Data
@AllArgsConstructor
public abstract class TargetedEvent
{

    /**
     * Creator of the action.
     */
    private Connection sender;
    /**
     * Receiver of the action.
     */
    private Connection receiver;
}
