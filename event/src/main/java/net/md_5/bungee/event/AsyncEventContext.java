package net.md_5.bungee.event;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AsyncEventContext<T extends AsyncEventBusEvent>
{

    private final T event;
    private final EventHandlerMethod[] handlers;
    private final EventBus eventBus;

    private int index = 0;

    public void post()
    {
        while ( index < handlers.length )
        {
            EventHandlerMethod handler = handlers[index];

            eventBus.executeEventHandler( event, handler );

            index++;

            // Stop current processing until completeIntent is called
            if ( event.isRegisteredIntent() )
            {
                return;
            }
        }

        // All handlers have been processed
        event.onComplete();
    }
}
