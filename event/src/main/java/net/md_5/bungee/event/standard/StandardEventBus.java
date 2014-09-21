package net.md_5.bungee.event.standard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.md_5.bungee.event.generic.GenericBakedEventBus;

public class StandardEventBus extends GenericBakedEventBus
{

    private final Map<Class<?>, EventHandlerMethod[]> byEventBaked = new HashMap<>();

    @Override
    public void post(Object event)
    {
        lock.readLock().lock();
        try
        {
            EventHandlerMethod[] handlers = byEventBaked.get( event.getClass() );
            if ( handlers != null )
            {
                for ( EventHandlerMethod method : handlers )
                {
                    try
                    {
                        method.invoke( event );
                    } catch ( IllegalAccessException ex )
                    {
                        throw new Error( "Method became inaccessible: " + event, ex );
                    } catch ( IllegalArgumentException ex )
                    {
                        throw new Error( "Method rejected target/argument: " + event, ex );
                    } catch ( InvocationTargetException ex )
                    {
                        logger.log( Level.WARNING, MessageFormat.format( "Error dispatching event {0} to listener {1}", event, method.getListener() ), ex.getCause() );
                    }
                }
            }
        } finally
        {
            lock.readLock().unlock();
        }
    }

    /**
     * Shouldn't be called without first locking the writeLock; intended for use
     * only inside {@link #register(java.lang.Object) register(Object)} or
     * {@link #unregister(java.lang.Object) unregister(Object)}.
     */
    public void bake(Class<?> eventClass)
    {
        Map<Byte, Map<Object, Method[]>> handlersByPriority = byListenerAndPriority.get( eventClass );
        if ( handlersByPriority != null )
        {
            List<EventHandlerMethod> handlersList = new ArrayList<>( handlersByPriority.size() * 2 );

            // Either I'm really tired, or the only way we can iterate between Byte.MIN_VALUE and Byte.MAX_VALUE inclusively,
            // with only a byte on the stack is by using a do {} while() format loop.
            byte value = Byte.MIN_VALUE;
            do
            {
                Map<Object, Method[]> handlersByListener = handlersByPriority.get( value );
                if ( handlersByListener != null )
                {
                    for ( Map.Entry<Object, Method[]> listenerHandlers : handlersByListener.entrySet() )
                    {
                        for ( Method method : listenerHandlers.getValue() )
                        {
                            EventHandlerMethod ehm = new EventHandlerMethod( listenerHandlers.getKey(), method );
                            handlersList.add( ehm );
                        }
                    }
                }
            } while ( value++ < Byte.MAX_VALUE );
            byEventBaked.put( eventClass, handlersList.toArray( new EventHandlerMethod[ handlersList.size() ] ) );
        } else
        {
            byEventBaked.put( eventClass, null );
        }
    }

    @Override
    public void bake()
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
}
