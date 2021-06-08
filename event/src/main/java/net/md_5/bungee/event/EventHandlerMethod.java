package net.md_5.bungee.event;

import java.util.function.BiConsumer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class EventHandlerMethod
{

    @Getter
    private final Object listener;
    @Getter
    private final BiConsumer<Object, Object> method;

    public void invoke(Object event)
    {
        method.accept( listener, event );
    }
}
