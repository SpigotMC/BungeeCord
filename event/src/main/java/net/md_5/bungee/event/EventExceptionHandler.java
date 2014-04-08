package net.md_5.bungee.event;

/**
 * Listener that gets notified when an event handler causes an error during event handling.
 */
public interface EventExceptionHandler
{

    void onException(Throwable exception, Object event, EventHandlerMethod handler);
}
