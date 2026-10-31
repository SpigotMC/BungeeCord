package net.md_5.bungee.api.event;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.ServerLink;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when the server the player is connected to sends server links.
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class ServerSendServerLinksEvent extends Event implements Cancellable
{

    /**
     * Cancelled status.
     */
    private boolean cancelled;

    /**
     * The player the links are sent to
     */
    private final ProxiedPlayer player;

    /**
     * The server sending the links
     */
    private final ServerInfo server;

    /**
     * The server links sent.
     */
    private List<ServerLink> serverLinks;

}
