package net.md_5.bungee.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor()
public class EventHandlerMethod
{

    @Getter
    private final Object listener;
    @Getter
    private final Method method;

    @Setter
    private Logger logger; //BotFilter

    public void invoke(Object event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        //method.invoke( listener, event );

        //BotFilter start
        long start = System.nanoTime();
        method.invoke( listener, event );
        long elapsed = System.nanoTime() - start;
        if ( elapsed > 230000000 )
        {
            logger.log( Level.WARNING, "Event {0} §rtook §c{1}ns§r to process for {2}!", new Object[]
            {
                event.getClass().getSimpleName(), elapsed, method
            } );
        }
        //BotFilter end
    }
}
