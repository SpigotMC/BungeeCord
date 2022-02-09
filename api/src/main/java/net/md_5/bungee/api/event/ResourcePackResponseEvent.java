package net.md_5.bungee.api.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.connection.Connection;

/**
 * Called when a client takes action on a resource pack request
 * sent to their client by the proxy or server.
 */
@Getter
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ResourcePackResponseEvent extends TargetedEvent
{

    /**
     * The response status
     */
    private final Response response;

    /**
     * The hash. <i>Note: this will be null in clients which are
     * 1.10 and later</i>
     */
    private final String hash;

    public ResourcePackResponseEvent(Connection sender, Connection receiver, Response response, String hash)
    {
        super( sender, receiver );
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
        ACCEPTED
    }
}
