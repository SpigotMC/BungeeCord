package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when a player takes action on a resource pack request sent by the
 * server.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ResourcePackResponseEvent extends TargetedEvent
{

    /**
     * Player sending the response.
     */
    private final ProxiedPlayer player;
    /**
     * The response status.
     */
    private final ResourcePackResponseEvent.Response response;
    /**
     * The hash, note this will be null in clients 1.10 or later
     */
    private final String hash;

    public ResourcePackResponseEvent(Connection sender, Connection receiver, ProxiedPlayer player, ResourcePackResponseEvent.Response response, String hash)
    {
        super( sender, receiver );
        this.player = player;
        this.response = response;
        this.hash = hash;
    }

    public enum Response
    {

        /**
         * The resource pack has been successfully downloaded and applied to the
         * client.
         */
        SUCCESSFULLY_LOADED,
        /**
         * The client refused to accept the resource pack.
         */
        DECLINED,
        /**
         * The client accepted the pack but download failed.
         */
        FAILED_DOWNLOAD,
        /**
         * The client accepted the pack and is beginning a download of it.
         */
        ACCEPTED;
    }
}
