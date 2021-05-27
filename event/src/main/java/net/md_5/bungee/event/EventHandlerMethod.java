package net.md_5.bungee.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class EventHandlerMethod
{

    @Getter
    private final Object listener;
    @Getter
    private final Consumer<Object> method;

    public void invoke(Object event)
    {
        method.accept( event );
    }
}
