package net.md_5.bungee.plugin;

import lombok.Data;
import net.md_5.bungee.UserConnection;

@Data
public class ChatEvent implements Cancellable
{

    /**
     * Canceled state.
     */
    private boolean cancelled;
    /**
     * Whether this packet is destined for the server or the client.
     */
    private final Destination destination;
    /**
     * User in question.
     */
    private final UserConnection connection;
    /**
     * Text contained in this chat.
     */
    private String text;

    /**
     * An enum that signifies the destination for this packet.
     */
    public enum Destination
    {

        SERVER,
        CLIENT
    }
}
