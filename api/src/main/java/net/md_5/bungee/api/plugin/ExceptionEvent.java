package net.md_5.bungee.api.plugin;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.event.EventHandlerMethod;

/**
 * Event that is submitted when a previous event handler caused an error.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public final class ExceptionEvent extends Event
{

    /**
     * The error that occured.
     */
    private final Throwable throwable;
    /**
     * The event that was being processed.
     */
    private final Event event;
    /**
     * The event handler that caused the error.
     */
    private final EventHandlerMethod handler;

    /**
     * Cancelled state.
     *
     * A cancelled ExceptionEvent will not be printed to the console.
     */
    @Setter
    private boolean cancelled = false;
}
