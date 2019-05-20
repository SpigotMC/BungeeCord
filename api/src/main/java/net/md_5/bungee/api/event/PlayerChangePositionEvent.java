/*
 * Copyright 2019 Ivan Pekov (MrIvanPlays)

 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 **/
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
