package net.md_5.bungee.event;

import java.lang.reflect.Method;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EventMethod implements Comparable<EventMethod>
{

    private final EventHandler handler;
    private final Method method;

    protected EventMethod(Method method)
    {
        this( method.getAnnotation( EventHandler.class ), method );
    }

    public int getPriority()
    {
        return handler.priority().getPriority();
    }

    @Override
    public int compareTo(EventMethod t)
    {
        return getPriority() - t.getPriority();
    }
}