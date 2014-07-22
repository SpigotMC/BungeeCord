package net.md_5.bungee.api.plugin;

import net.md_5.bungee.event.EventHandlerMethod;
import net.md_5.bungee.event.LoggingExceptionHandler;

import java.util.logging.Logger;

/**
 * An EventExceptionHandler that will call an ExceptionEvent when an error occurs.
 */
public class NotifyingExceptionHandler extends LoggingExceptionHandler
{

    private final PluginManager pluginManager;

    public NotifyingExceptionHandler(Logger logger, PluginManager pluginManager)
    {
        super( logger );
        this.pluginManager = pluginManager;
    }

    @Override
    public void onException(Throwable ex, Object event, EventHandlerMethod method)
    {
        // Check if this actually is an event. It should always be one but it doesn't hurt to check.
        // Additionally, avoid a stack overflow when an ExceptionEvent handler causes an error.
        if ( event instanceof Event && !( event instanceof ExceptionEvent ) )
        {
            ExceptionEvent notification = pluginManager.callEvent( new ExceptionEvent( ex, (Event) event, method ) );
            if ( notification.isCancelled() )
            {
                // The event was cancelled so we should not print this error.
                return;
            }
        }
        // This is either an ExceptionEvent already or the ExceptionEvent was not cancelled so we should use the LoggingExceptionHandler to print it to console.
        super.onException( ex, event, method );
    }
}
