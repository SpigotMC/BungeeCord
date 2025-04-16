package net.md_5.bungee.event;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventBus
{

    private final Map<Class<?>, Map<Byte, Map<Object, Method[]>>> byListenerAndPriority = new HashMap<>();
    private final Map<Class<?>, EventHandlerMethod[]> byEventBaked = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();
    private final Logger logger;

    public EventBus()
    {
        this( null );
    }

    public EventBus(Logger logger)
    {
        this.logger = ( logger == null ) ? Logger.getLogger( Logger.GLOBAL_LOGGER_NAME ) : logger;
    }

    public void post(Object event)
    {
        EventHandlerMethod[] handlers = byEventBaked.get( event.getClass() );

        if ( handlers != null )
        {
            for ( EventHandlerMethod method : handlers )
            {
                long start = System.nanoTime();

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

                long elapsed = System.nanoTime() - start;
                if ( elapsed > 50000000 )
                {
                    logger.log( Level.WARNING, "Plugin listener {0} took {1}ms to process event {2}!", new Object[]
                    {
                        method.getListener().getClass().getName(), elapsed / 1000000, event
                    } );
                }
            }
        }
    }

    private Map<Class<?>, Map<Byte, Set<Method>>> findHandlers(Object listener)
    {
        Map<Class<?>, Map<Byte, Set<Method>>> handler = new HashMap<>();
        Set<Method> methods = ImmutableSet.<Method>builder().add( listener.getClass().getMethods() ).add( listener.getClass().getDeclaredMethods() ).build();
        for ( final Method m : methods )
        {
            EventHandler annotation = m.getAnnotation( EventHandler.class );
            if ( annotation != null )
            {
                Class<?>[] params = m.getParameterTypes();
                if ( params.length != 1 )
                {
                    logger.log( Level.INFO, "Method {0} in class {1} annotated with {2} does not have single argument", new Object[]
                    {
                        m, listener.getClass(), annotation
                    } );
                    continue;
                }
                Map<Byte, Set<Method>> prioritiesMap = handler.computeIfAbsent( params[0], k -> new HashMap<>() );
                Set<Method> priority = prioritiesMap.computeIfAbsent( annotation.priority(), k -> new HashSet<>() );
                priority.add( m );
            }
        }
        return handler;
    }

    public void register(Object listener)
    {
        Map<Class<?>, Map<Byte, Set<Method>>> handler = findHandlers( listener );
        lock.lock();
        try
        {
            for ( Map.Entry<Class<?>, Map<Byte, Set<Method>>> e : handler.entrySet() )
            {
                Map<Byte, Map<Object, Method[]>> prioritiesMap = byListenerAndPriority.computeIfAbsent( e.getKey(), k -> new HashMap<>() );
                for ( Map.Entry<Byte, Set<Method>> entry : e.getValue().entrySet() )
                {
                    Map<Object, Method[]> currentPriorityMap = prioritiesMap.computeIfAbsent( entry.getKey(), k -> new HashMap<>() );
                    currentPriorityMap.put( listener, entry.getValue().toArray( new Method[ 0 ] ) );
                }
                bakeHandlers( e.getKey() );
            }
        } finally
        {
            lock.unlock();
        }
    }

    public void unregister(Object listener)
    {
        Map<Class<?>, Map<Byte, Set<Method>>> handler = findHandlers( listener );
        lock.lock();
        try
        {
            for ( Map.Entry<Class<?>, Map<Byte, Set<Method>>> e : handler.entrySet() )
            {
                Map<Byte, Map<Object, Method[]>> prioritiesMap = byListenerAndPriority.get( e.getKey() );
                if ( prioritiesMap != null )
                {
                    for ( Byte priority : e.getValue().keySet() )
                    {
                        Map<Object, Method[]> currentPriority = prioritiesMap.get( priority );
                        if ( currentPriority != null )
                        {
                            currentPriority.remove( listener );
                            if ( currentPriority.isEmpty() )
                            {
                                prioritiesMap.remove( priority );
                            }
                        }
                    }
                    if ( prioritiesMap.isEmpty() )
                    {
                        byListenerAndPriority.remove( e.getKey() );
                    }
                }
                bakeHandlers( e.getKey() );
            }
        } finally
        {
            lock.unlock();
        }
    }

    /**
     * Shouldn't be called without first locking the writeLock; intended for use
     * only inside {@link #register(java.lang.Object) register(Object)} or
     * {@link #unregister(java.lang.Object) unregister(Object)}.
     *
     * @param eventClass event class
     */
    private void bakeHandlers(Class<?> eventClass)
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
            byEventBaked.put( eventClass, handlersList.toArray( new EventHandlerMethod[ 0 ] ) );
        } else
        {
            byEventBaked.remove( eventClass );
        }
    }
}
