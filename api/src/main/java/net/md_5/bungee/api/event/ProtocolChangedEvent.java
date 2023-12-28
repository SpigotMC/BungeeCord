package net.md_5.bungee.api.event;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.protocol.Protocol;

/**
 * Called when the encode or decode protocol of a {@link Connection} is changed.
 */
@Getter
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class ProtocolChangedEvent extends Event
{

    /**
     * The old protocol.
     */
    private Protocol oldProtocol;

    /**
     * The new protocol.
     */
    private Protocol newProtocol;

    /**
     * The connection whose protocol is being changed.
     */
    private Connection connection;

    /**
     * The direction of the changed protocol.
     * encode or decode
     */
    private Direction direction;

    public enum Direction
    {
        ENCODE,
        DECODE
    }
}
