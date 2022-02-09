package net.md_5.bungee.api.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when a server sends a request for a resource pack to
 * the client
 */
@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
public class ResourcePackRequestEvent extends Event implements Cancellable
{

    /**
     * Cancelled state.
     */
    @Setter
    private boolean cancelled;

    /**
     * The one who sent the request.
     */
    private final Connection sender;

    /**
     * Player receiving the response.
     */
    private final ProxiedPlayer player;

    /**
     * The URL of resource pack.
     */
    private final String url;

    /**
     * The hash of resource pack.
     */
    private final String hash;

    /**
     * Returns whether the notchian client will be forced to use
     * the resource pack from the server. If they decline they
     * will be kicked from the server.
     * <i>Note: this will always return false for clients which
     * are older than 1.16 (1.16 included)</i>
     */
    private final boolean forced;

    /**
     * This is the message shown in the prompt making the client
     * accept or decline the resource pack. <i>Note: this will
     * always be null for clients which are older than 1.16 (1.16
     * included)</i>
     */
    private final BaseComponent[] promptMessage;
}
