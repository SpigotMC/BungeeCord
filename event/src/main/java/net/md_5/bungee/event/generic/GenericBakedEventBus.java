package net.md_5.bungee.event.generic;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.event.BakedEventBus;
import net.md_5.bungee.event.EventHandler;

public abstract class GenericBakedEventBus implements BakedEventBus
{

    @Getter
    @Setter
    private boolean autoBake = true;
    //
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected final Map<Class<?>, Map<Byte, Map<Object, Method[]>>> byListenerAndPriority = new HashMap<>();
    //
    protected final Logger logger;

    public GenericBakedEventBus()
    {
        this( null );
    }

    public GenericBakedEventBus(Logger logger)
    {
        this.logger = ( logger == null ) ? Logger.getGlobal() : logger;
    }

    private Map<Class<?>, Map<Byte, Set<Method>>> findHandlers(Object listener)
    {
        Map<Class<?>, Map<Byte, Set<Method>>> handler = new HashMap<>();
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
                Map<Byte, Set<Method>> prioritiesMap = handler.get( params[0] );
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

    @Override
    public void register(Object listener)
    {
        Map<Class<?>, Map<Byte, Set<Method>>> handler = findHandlers( listener );
        lock.writeLock().lock();
        try
        {
            for ( Map.Entry<Class<?>, Map<Byte, Set<Method>>> e : handler.entrySet() )
            {
                Map<Byte, Map<Object, Method[]>> prioritiesMap = byListenerAndPriority.get( e.getKey() );
                if ( prioritiesMap == null )
                {
                    prioritiesMap = new HashMap<>();
                    byListenerAndPriority.put( e.getKey(), prioritiesMap );
                }
                for ( Map.Entry<Byte, Set<Method>> entry : e.getValue().entrySet() )
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
                bake( e.getKey() );
            }
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void unregister(Object listener)
    {
        Map<Class<?>, Map<Byte, Set<Method>>> handler = findHandlers( listener );
        lock.writeLock().lock();
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
                bake( e.getKey() );
            }
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    @Override
    public abstract void bake();

    @Override
    public abstract void bake(Class<?> eventClass);

    @Override
    public abstract void post(Object event);

}
