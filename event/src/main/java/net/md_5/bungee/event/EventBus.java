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

    private final Map<Class<?>, Map<Object, Map<EventPriority,Method[]>>> eventToHandler = new HashMap<>();
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
            Map<Object, Map<EventPriority, Method[]>> handlers = eventToHandler.get( event.getClass() );
            if ( handlers != null )
            {
                for ( Map.Entry<Object, Map<EventPriority, Method[]>> handler : handlers.entrySet() )
                {
                    for(EventPriority ep:EventPriority.values())
                    {
                        Method[] methods=handler.getValue().get( ep );
                        if(methods==null)
                        {
                            continue;
                        }
                        for ( Method method : methods )
                        {
                            try
                            {
                                // at this point we check if the method ignores cancelled events
                                boolean execute=true;
                                if (event instanceof Cancellable){
                                    if (((Cancellable) event).isCancelled() && method.getAnnotation( EventHandler.class ).ignoreCancelled()){
                                        execute=false;
                                    }
                                }

                                // only invoke if exection is allowed
                                if(execute){
                                     method.invoke( handler.getKey(), event );
                                }
                               
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
        Map<Class<?>, Map<EventPriority,Set<Method>>> handler = new HashMap<>();
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
                
                Map<EventPriority,Set<Method>> existingMap = handler.get( params[0] );
                if ( existingMap == null )
                {
                    existingMap = new HashMap<>();
                    handler.put( params[0], existingMap );
                }
                Set<Method> existing = existingMap.get( annotation.priority() );
                if ( existing == null )
                {
                    existing = new HashSet<>();
                    existingMap.put( annotation.priority(), existing );
                }
                existing.add( m );
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
                Map<Object, Map<EventPriority, Method[]>> a = eventToHandler.get( e.getKey() );
                if ( a == null )
                {
                    a = new HashMap<>();
                    eventToHandler.put( e.getKey(), a );
                }
                
                Map<EventPriority, Method[]> b = a.get( listener );
                if ( b == null )
                {
                    b = new HashMap<>();
                    a.put( listener, b );
                }
                for(Map.Entry<EventPriority, Set<Method>> entry:e.getValue().entrySet())
                {
                    Method[] baked = new Method[ entry.getValue().size() ];
                    b.put( entry.getKey(), entry.getValue().toArray( baked ) );
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
                Map<Object, Map<EventPriority, Method[]>> a = eventToHandler.get( e.getKey() );
                if ( a != null )
                {
                    a.remove( listener );
                    if ( a.isEmpty() )
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
