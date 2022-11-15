package net.md_5.bungee.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class EventHandlerMethod
{

    @Getter
    private final Object listener;
    private final Consumer<Object> method;

    public void invoke(Object event) throws Throwable
    {
        method.accept( event );
    }

}
