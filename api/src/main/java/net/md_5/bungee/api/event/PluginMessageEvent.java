package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.Connection;
import net.md_5.bungee.api.plugin.Cancellable;

/**
 * Event called when a plugin message is sent to the client or server.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PluginMessageEvent extends TargetedEvent implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Tag specified for this plugin message.
     */
    private String tag;
    /**
     * Data contained in this plugin message.
     */
    private byte[] data;

    public PluginMessageEvent(Connection sender, Connection receiver, String tag, byte[] data)
    {
        super(sender, receiver);
        this.tag = tag;
        this.data = data;
    }
}
