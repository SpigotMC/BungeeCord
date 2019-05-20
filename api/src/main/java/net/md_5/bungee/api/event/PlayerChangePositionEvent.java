package net.md_5.bungee.api.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.Position;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event, called when player changes his position
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Getter
public class PlayerChangePositionEvent extends Event implements Cancellable
{

    /**
     * Returns whenever this event is cancelled
     */
    @Setter
    private boolean cancelled;

    /**
     * The player, who is changing his position
     */
    private ProxiedPlayer player;

    /**
     * The old position the player was located
     */
    private Position oldPosition;

    /**
     * The new position the player is located when the event gets called
     */
    @Setter
    private Position newPosition;

    public PlayerChangePositionEvent(ProxiedPlayer player, Position oldPosition, Position newPosition)
    {
        this.player = player;
        this.oldPosition = oldPosition;
        this.newPosition = newPosition;
    }
}
