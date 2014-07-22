package net.md_5.bungee.event;

import lombok.RequiredArgsConstructor;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An EventExceptionHandler that logs the error to the logger (console).
 */
@RequiredArgsConstructor
public class LoggingExceptionHandler implements EventExceptionHandler
{

    private final Logger logger;

    @Override
    public void onException(Throwable ex, Object event, EventHandlerMethod method)
    {
        logger.log( Level.WARNING, MessageFormat.format( "Error dispatching event {0} to listener {1}", event, method.getListener() ), ex );
    }
}
