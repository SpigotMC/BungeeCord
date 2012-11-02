package net.md_5.bungee.plugin;

import lombok.Data;
import net.md_5.bungee.UserConnection;

/**
 * Event called when the decision is made to decide which server to connect to.
 */
@Data
public class ServerConnectEvent
{

    /**
     * If the player currently has no server, this is true
     */
    private final boolean firstTime;
    /**
     * Message to send just before the change.
     * null for no message
     */
    private String message;
    /**
     * User in question.
     */
    private final UserConnection connection;
    /**
     * Name of the server they are connecting to.
     */
    private final String server;
    /**
     * Name of the server which they will be forwarded to instead.
     */
    private String newServer;
}
