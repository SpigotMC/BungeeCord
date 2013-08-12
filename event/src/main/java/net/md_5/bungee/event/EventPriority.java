package net.md_5.bungee.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Importance of the {@link EventHandler}.
 * Before executioning an Event all current Handlers are sorted by their Priority.
 */
@AllArgsConstructor
public enum EventPriority {
    /**
     * Make these EventHandlers to be executed first.
     * Allowing other Handlers to perform changes upon.
     */
    LOWEST(0),
    /**
     * 
     */
    LOW(1),
    /**
     * Default EventPriorty
     */
    NORMAL(2),
    /**
     * Higher EventPriorty
     */
    HIGH(3),
    /**
     * Most important EventPriorty for changes
     */
    HIGHEST(4),
    /**
     * Logging EventPriority.
     * <b>Do not change anything with this priority!</b>
     * This Priority is for logging purpose only.
     */
    MONITOR(5);

    @Getter
    private final int priority;
}