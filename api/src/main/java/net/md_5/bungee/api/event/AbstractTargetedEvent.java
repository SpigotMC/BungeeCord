package net.md_5.bungee.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.AbstractEvent;

/**
 * An event which occurs in the communication between two nodes.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractTargetedEvent extends AbstractEvent
{

    /**
     * Creator of the action.
     */
    private final Connection sender;
    /**
     * Receiver of the action.
     */
    private final Connection receiver;
}
