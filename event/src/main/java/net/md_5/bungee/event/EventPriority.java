package net.md_5.bungee.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Importance of the {@link EventHandler}. When executing an Event, the handlers
 * are called in order of their Priority.
 */
@AllArgsConstructor
public enum EventPriority
{

    /**
     * Lowest EventPriority. Use this priority to allow other plugins to further
     * customize the outcome.
     */
    LOWEST( 0 ),
    /**
     * Higher than lowest, lower than normal.
     */
    LOW( 1 ),
    /**
     * Default EventPriority
     */
    NORMAL( 2 ),
    /**
     * High EventPriority. Use this priority to have more verdict on the
     * outcome.
     */
    HIGH( 3 ),
    /**
     * Most important EventPriorty for changes. Use this priority to have
     * absolute verdict of the outcome of this event.
     */
    HIGHEST( 4 ),
    /**
     * Logging/Monitor EventPriority. This priority is for <b>read only</b>
     * event handlers. Do not change the outcome of the event in this priority.
     * Intended for logging purposes.
     */
    MONITOR( 5 );
    @Getter
    private final int priority;
}
