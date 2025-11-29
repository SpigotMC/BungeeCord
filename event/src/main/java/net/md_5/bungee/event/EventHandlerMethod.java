package net.md_5.bungee.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import lombok.Getter;

public class EventHandlerMethod
{

    @Getter
    private final Object listener;
    private final MethodHandle methodHandle;

    public EventHandlerMethod(Object listener, Method method) throws IllegalAccessException
    {
        this.listener = listener;
        this.methodHandle = MethodHandles.lookup()
            .unreflect( method )
            .bindTo( listener )
            .asType( MethodType.methodType( void.class, Object.class ) );
    }

    public void invoke(Object event) throws Throwable
    {
        methodHandle.invokeExact( event );
    }
}
