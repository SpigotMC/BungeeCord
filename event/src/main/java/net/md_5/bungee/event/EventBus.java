package net.md_5.bungee.event;

import com.google.common.collect.ImmutableSet;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

public class EventBus
{

    private Function<Class<?>, MethodHandles.Lookup> LOOKUP_FUNCTION;

    @IgnoreJRERequirement
    private void initLookup()
    {
        try
        {
            // We grab access to a Lookup with all privileges.
            // This is required as we have to call Lookup#in to be in the plugin's ClassLoader context. Only this
            // full-power lookup instance can have private and module access after call to Lookup#in, which we need for
            // LambdaMetafactory. Even the java 9+ method MethodHandles.privateLookupIn does not give us MODULE access
            // when we call it if its present (via reflection).
            // This still requires a jvm argument on java 9+ though: --add-opens java.base/java.lang.invoke=ALL-UNNAMED
            Field field = MethodHandles.Lookup.class.getDeclaredField( "IMPL_LOOKUP" );
            field.setAccessible( true );
            MethodHandles.Lookup lookup = (MethodHandles.Lookup) field.get( null );
            LOOKUP_FUNCTION = lookup::in;
        } catch ( Throwable t )
        {
            if ( t.getClass().getName().equals( "java.lang.reflect.InaccessibleObjectException" ) )
            {
                logger.log( Level.SEVERE, "Using BungeeCord on Java 9 or higher requires you to add the jvm argument --add-opens java.base/java.lang.invoke=ALL-UNNAMED (before -jar)" );
            }
            throw new RuntimeException( t );
        }
    }

    private final Map<Class<?>, Map<Byte, Map<Object, Consumer<Object>[]>>> byListenerAndPriority = new HashMap<>();
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
        initLookup();
    }

    public void post(Object event)
    {
        EventHandlerMethod[] handlers = byEventBaked.get( event.getClass() );

        if ( handlers != null )
        {
            for ( EventHandlerMethod method : handlers )
            {
                try
                {
                    method.invoke( event );
                } catch ( Throwable t )
                {
                    logger.log( Level.WARNING, MessageFormat.format( "Error dispatching event {0} to listener {1}", event, method.getListener() ), t );
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
                    logger.log( Level.INFO, "Method {0} in class {1} annotated with {2} does not have exactly one argument", new Object[]
                    {
                        m, listener.getClass(), annotation
                    } );
                    continue;
                }
                Map<Byte, Set<Method>> prioritiesMap = handler.computeIfAbsent( params[ 0 ], k -> new HashMap<>() );
                Set<Method> priority = prioritiesMap.computeIfAbsent( annotation.priority(), k -> new HashSet<>() );
                priority.add( m );
            }
        }
        return handler;
    }

    @IgnoreJRERequirement
    @SuppressWarnings("unchecked")
    private Consumer<Object> createMethodInvoker(Object listener, Method method)
    {
        try
        {
            MethodHandles.Lookup lookup = LOOKUP_FUNCTION.apply( listener.getClass() );
            return (Consumer<Object>) LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    MethodType.methodType( Consumer.class, listener.getClass() ),
                    MethodType.methodType( method.getReturnType(), Object.class ),
                    lookup.unreflect( method ),
                    MethodType.methodType( method.getReturnType(), method.getParameterTypes()[ 0 ] )
            ).getTarget().invoke( listener );
        } catch ( Throwable t )
        {
            throw new RuntimeException( "Could not create invoker for method " + method + " of listener " + listener + " (" + listener.getClass() + ")", t );
        }
    }

    @SuppressWarnings("unchecked")
    public void register(Object listener)
    {
        Map<Class<?>, Map<Byte, Set<Method>>> handler = findHandlers( listener );
        lock.lock();
        try
        {
            for ( Map.Entry<Class<?>, Map<Byte, Set<Method>>> e : handler.entrySet() )
            {
                Map<Byte, Map<Object, Consumer<Object>[]>> prioritiesMap = byListenerAndPriority.computeIfAbsent( e.getKey(), k -> new HashMap<>() );
                for ( Map.Entry<Byte, Set<Method>> entry : e.getValue().entrySet() )
                {
                    if ( entry.getValue().isEmpty() )
                    {
                        continue;
                    }
                    Consumer<Object>[] baked = entry.getValue().stream()
                            .map( method -> createMethodInvoker( listener, method ) )
                            .toArray( Consumer[]::new );
                    prioritiesMap.computeIfAbsent( entry.getKey(), k -> new HashMap<>() ).put( listener, baked );
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
                Map<Byte, Map<Object, Consumer<Object>[]>> prioritiesMap = byListenerAndPriority.get( e.getKey() );
                if ( prioritiesMap != null )
                {
                    for ( Byte priority : e.getValue().keySet() )
                    {
                        Map<Object, Consumer<Object>[]> currentPriority = prioritiesMap.get( priority );
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
        Map<Byte, Map<Object, Consumer<Object>[]>> handlersByPriority = byListenerAndPriority.get( eventClass );
        if ( handlersByPriority != null )
        {
            List<EventHandlerMethod> handlersList = new ArrayList<>( handlersByPriority.size() * 2 );

            // Either I'm really tired, or the only way we can iterate between Byte.MIN_VALUE and Byte.MAX_VALUE inclusively,
            // with only a byte on the stack is by using a do {} while() format loop.
            byte value = Byte.MIN_VALUE;
            do
            {
                Map<Object, Consumer<Object>[]> handlersByListener = handlersByPriority.get( value );
                if ( handlersByListener != null )
                {
                    for ( Map.Entry<Object, Consumer<Object>[]> listenerHandlers : handlersByListener.entrySet() )
                    {
                        Object listener = listenerHandlers.getKey();
                        for ( Consumer<Object> method : listenerHandlers.getValue() )
                        {
                            EventHandlerMethod ehm = new EventHandlerMethod( listener, method );
                            handlersList.add( ehm );
                        }
                    }
                }
            } while ( value++ < Byte.MAX_VALUE );
            byEventBaked.put( eventClass, handlersList.toArray( new EventHandlerMethod[ handlersList.size() ] ) );
        } else
        {
            byEventBaked.remove( eventClass );
        }
    }
}
