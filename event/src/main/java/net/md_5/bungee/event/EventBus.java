package net.md_5.bungee.event;

import java.lang.annotation.Annotation;
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

    private final Map<Class<?>, Map<Object, Method[]>> eventToHandler = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Logger logger;
    private final Class<? extends Annotation>[] annotations;

    public EventBus()
    {
        this( null, (Class<? extends Annotation>[]) null );
    }

    public EventBus(Logger logger)
    {
        this( logger, (Class<? extends Annotation>[]) null );
    }

    @SuppressWarnings("unchecked")
    public EventBus(Class<? extends Annotation>... annotations)
    {
        this( null, annotations );
    }

    @SuppressWarnings("unchecked")
    public EventBus(Logger logger, Class<? extends Annotation>... annotations)
    {
        this.logger = ( logger == null ) ? Logger.getGlobal() : logger;
        this.annotations = ( annotations == null || annotations.length == 0 ) ? new Class[]
        {
            EventHandler.class
        } : annotations;
    }

    public void post(Object event)
    {
        lock.readLock().lock();
        try
        {
            Map<Object, Method[]> handlers = eventToHandler.get( event.getClass() );
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
        } finally
        {
            lock.readLock().unlock();
        }
    }

    private Map<Class<?>, Set<Method>> findHandlers(Object listener)
    {
        Map<Class<?>, Set<Method>> handler = new HashMap<>();
        for ( Method m : listener.getClass().getDeclaredMethods() )
        {
            for ( Class<? extends Annotation> annotation : annotations )
            {
                if ( m.isAnnotationPresent( annotation ) )
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

                    Set<Method> existing = handler.get( params[0] );
                    if ( existing == null )
                    {
                        existing = new HashSet<>();
                        handler.put( params[0], existing );
                    }
                    existing.add( m );
                    break;
                }
            }
        }
        return handler;
    }

    public void register(Object listener)
    {
        Map<Class<?>, Set<Method>> handler = findHandlers( listener );
        lock.writeLock().lock();
        try
        {
            for ( Map.Entry<Class<?>, Set<Method>> e : handler.entrySet() )
            {
                Map<Object, Method[]> a = eventToHandler.get( e.getKey() );
                if ( a == null )
                {
                    a = new HashMap<>();
                    eventToHandler.put( e.getKey(), a );
                }
                Method[] baked = new Method[ e.getValue().size() ];
                a.put( listener, e.getValue().toArray( baked ) );
            }
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    public void unregister(Object listener)
    {
        Map<Class<?>, Set<Method>> handler = findHandlers( listener );
        lock.writeLock().lock();
        try
        {
            for ( Map.Entry<Class<?>, Set<Method>> e : handler.entrySet() )
            {
                Map<Object, Method[]> a = eventToHandler.get( e.getKey() );
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
