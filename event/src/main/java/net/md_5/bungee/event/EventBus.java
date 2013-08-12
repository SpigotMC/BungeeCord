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
import net.md_5.bungee.api.plugin.Cancellable;

public class EventBus
{

    private final Map<Class<?>, Map<Object, EventMethod[]>> eventToHandler = new HashMap<>();
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
            Map<Object, EventMethod[]> handlers = eventToHandler.get( event.getClass() );
            if ( handlers != null )
            {
                Cancellable c = null;
                if ( event instanceof Cancellable )
                {
                    c = (Cancellable) event;
                }
                for ( Map.Entry<Object, EventMethod[]> handler : handlers.entrySet() )
                {
                    for ( EventMethod method : handler.getValue() )
                    {
                        if ( c != null && c.isCancelled() )
                        {
                            if ( !method.getHandler().ignoreCancelled() )
                            {
                                continue;
                            }
                        }
                        try
                        {
                            method.getMethod().invoke( handler.getKey(), event );
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
        } finally
        {
            lock.readLock().unlock();
        }
    }

    private Map<Class<?>, Set<EventMethod>> findHandlers(Object listener)
    {
        Map<Class<?>, Set<EventMethod>> handler = new HashMap<>();
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

                Set<EventMethod> existing = handler.get( params[0] );
                if ( existing == null )
                {
                    existing = new HashSet<>();
                    handler.put( params[0], existing );
                }
                existing.add( new EventMethod( annotation, m ) );
            }
        }
        return handler;
    }

    public void register(Object listener)
    {
        Map<Class<?>, Set<EventMethod>> handler = findHandlers( listener );
        lock.writeLock().lock();
        try
        {
            for ( Map.Entry<Class<?>, Set<EventMethod>> e : handler.entrySet() )
            {
                Map<Object, EventMethod[]> a = eventToHandler.get( e.getKey() );
                if ( a == null )
                {
                    a = new HashMap<>();
                    eventToHandler.put( e.getKey(), a );
                }
                EventMethod[] baked = new EventMethod[ e.getValue().size() ];
                a.put( listener, PrioritySortingHandler.sort( e.getValue().toArray( baked ) ) );
            }
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    public void unregister(Object listener)
    {
        Map<Class<?>, Set<EventMethod>> handler = findHandlers( listener );
        lock.writeLock().lock();
        try
        {
            for ( Map.Entry<Class<?>, Set<EventMethod>> e : handler.entrySet() )
            {
                Map<Object, EventMethod[]> a = eventToHandler.get( e.getKey() );
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
