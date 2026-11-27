package net.md_5.bungee.api.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when the proxy dispatched a command.
 */
@Getter
@Setter
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ProxyCommandEvent extends Event implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Creator of the action.
     */
    private final CommandSender sender;
    /**
     * The command that will be executed
     */
    private String command;

    public ProxyCommandEvent(CommandSender sender, String command)
    {
        this.sender = sender;
        this.command = command;
    }

    @Override
    public boolean isCancelled()
    {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel)
    {
        this.cancelled = cancel;
    }
}
