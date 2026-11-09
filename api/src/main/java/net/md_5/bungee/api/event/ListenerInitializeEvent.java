package net.md_5.bungee.api.event;

import lombok.Data;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Event;

/**
 * This event is fired once a listener for player connections has finished loading.<br>
 * Its fired per listener, so multiple events are possible, if multiple ports are setup.<br>
 * This provides you access to:
 * <ul>
 * <li>The listeners state</li>
 * <li>The listeners info</li>
 * </ul>
 */
@Data
public class ListenerInitializeEvent extends Event
{
    private final ListenerInfo info;

    private final boolean success;
}
