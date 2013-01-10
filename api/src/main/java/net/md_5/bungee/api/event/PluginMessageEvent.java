package net.md_5.bungee.api.event;

import lombok.Data;
import net.md_5.bungee.api.Connection;
import net.md_5.bungee.api.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;

/**
 * Event called when a plugin message is sent to the client or server.
 */
@Data
public class PluginMessageEvent implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Dispatcher of this message.
     */
    private final Connection sender;
    /**
     * Player involved with sending or receiving this message.
     */
    private final ProxiedPlayer player;
    /**
     * Tag specified for this plugin message.
     */
    private String tag;
    /**
     * Data contained in this plugin message.
     */
    private byte[] data;
}
