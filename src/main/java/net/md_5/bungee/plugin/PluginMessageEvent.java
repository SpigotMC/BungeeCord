package net.md_5.bungee.plugin;

import lombok.Data;
import net.md_5.bungee.UserConnection;

/**
 * Event called when the decision is made to decide which server to connect to.
 */
@Data
public class PluginMessageEvent implements Cancellable
{
    /**
     * Canceled state.
     */
    private boolean cancelled;
    /**
     * Message to use when kicking if this event is canceled.
     */
    private String cancelReason;
    /**
     * Whether this packet is destined for the server or the client
     */
    private final Destination destination;
    /**
     * User in question
     */
    private final UserConnection connection;
    /**
     * Tag specified for this plugin message.
     */
    private String tag;
    /**
     * Data contained in this plugin message.
     */
    private String data;

    /**
     * An enum that signifies the destination for this packet
     */
    public enum Destination
    {
        SERVER,
        CLIENT
    }
}
