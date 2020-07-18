package net.md_5.bungee.api.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Cancellable;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a plugin message is sent to the client or server.
 */
@Getter
@Setter
@ToString(callSuper = true, exclude = "data")
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
    @NotNull
    private final String tag;
    /**
     * Data contained in this plugin message.
     */
    @NotNull
    private final byte[] data;

    public PluginMessageEvent(Connection sender, Connection receiver, @NotNull String tag, byte[] data)
    {
        super( sender, receiver );
        this.tag = tag;
        this.data = data;
    }
}
