package net.md_5.bungee.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventBus
{

    private final Map<Class<?>, Map<EventPriority, Map<Object, Method[]>>> eventToHandler = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Logger logger;

    public EventBus()
    {
        this( null );
    }

    public EventBus(Logger logger)
    {
        this.logger = ( logger == null ) ? Logger.getGlobal() : logger;
    }

    public void post(Object event)
    {
        lock.readLock().lock();
        try
        {
            Map<EventPriority, Map<Object, Method[]>> handlersByPriority = eventToHandler.get( event.getClass() );
            for ( EventPriority value : EventPriority.values() )
            {
                Map<Object, Method[]> handlers = handlersByPriority.get( value );
                if ( handlers != null )
                {
                    for ( Map.Entry<Object, Method[]> handler : handlers.entrySet() )
                    {
                        for ( Method method : handler.getValue() )
                        {
                            try
                            {
                                method.invoke( handler.getKey(), event );
                            } catch ( IllegalAccessException ex )
                            {
                                throw new Error( "Method became inaccessible: " + event, ex );
                            } catch ( IllegalArgumentException ex )
                            {
                                throw new Error( "Method rejected target/argument: " + event, ex );
                            } catch ( InvocationTargetException ex )
                            {
                                logger.log( Level.WARNING, MessageFormat.format( "Error dispatching event {0} to listener {1}", event, handler.getKey() ), ex.getCause() );
                            }
                        }
                    }
                }
            }
        } finally
        {
            lock.readLock().unlock();
        }
    }

    private Map<Class<?>, Map<EventPriority, Set<Method>>> findHandlers(Object listener)
    {
        Map<Class<?>, Map<EventPriority, Set<Method>>> handler = new HashMap<>();
        for ( Method m : listener.getClass().getDeclaredMethods() )
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
                Map<EventPriority, Set<Method>> prioritiesMap = handler.get( params[0] );
                if ( prioritiesMap == null )
                {
                    prioritiesMap = new HashMap<>();
                    handler.put( params[0], prioritiesMap );
                }
                Set<Method> priority = prioritiesMap.get( annotation.priority() );
                if ( priority == null )
                {
                    priority = new HashSet<>();
                    prioritiesMap.put( annotation.priority(), priority );
                }
                priority.add( m );
            }
        }
        return handler;
    }

    public void register(Object listener)
    {
        Map<Class<?>, Map<EventPriority, Set<Method>>> handler = findHandlers( listener );
        lock.writeLock().lock();
        try
        {
            for ( Map.Entry<Class<?>, Map<EventPriority, Set<Method>>> e : handler.entrySet() )
            {
                Map<EventPriority, Map<Object, Method[]>> prioritiesMap = eventToHandler.get( e.getKey() );
                if ( prioritiesMap == null )
                {
                    prioritiesMap = new HashMap<>();
                    eventToHandler.put( e.getKey(), prioritiesMap );
                }
                for ( Map.Entry<EventPriority, Set<Method>> entry : e.getValue().entrySet() )
                {
                    Map<Object, Method[]> currentPriorityMap = prioritiesMap.get( entry.getKey() );
                    if ( currentPriorityMap == null )
                    {
                        currentPriorityMap = new HashMap<>();
                        prioritiesMap.put( entry.getKey(), currentPriorityMap );
                    }
                    Method[] baked = new Method[ entry.getValue().size() ];
                    currentPriorityMap.put( listener, entry.getValue().toArray( baked ) );
                }
            }
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    public void unregister(Object listener)
    {
        Map<Class<?>, Map<EventPriority, Set<Method>>> handler = findHandlers( listener );
        lock.writeLock().lock();
        try
        {
            for ( Map.Entry<Class<?>, Map<EventPriority, Set<Method>>> e : handler.entrySet() )
            {
                Map<EventPriority, Map<Object, Method[]>> prioritiesMap = eventToHandler.get( e.getKey() );
                if ( prioritiesMap != null )
                {
                    for ( EventPriority priority : e.getValue().keySet() )
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
                        eventToHandler.remove( e.getKey() );
                    }
                }
            }
        } finally
        {
            lock.writeLock().unlock();
        }
    }
}
