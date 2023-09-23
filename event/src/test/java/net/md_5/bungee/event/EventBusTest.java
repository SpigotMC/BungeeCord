package net.md_5.bungee.event;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;

public class EventBusTest
{

    private final EventBus bus = new EventBus();
    private final CountDownLatch latch = new CountDownLatch( 2 );

    @Test
    public void testNestedEvents()
    {
        bus.register( this );
        bus.post( new FirstEvent() );
        assertEquals( 0, latch.getCount() );
    }

    @EventHandler
    public void firstListener(FirstEvent event)
    {
        bus.post( new SecondEvent() );
        assertEquals( 1, latch.getCount() );
        latch.countDown();
    }

    @EventHandler
    public void secondListener(SecondEvent event)
    {
        latch.countDown();
    }

    public static class FirstEvent
    {
    }

    public static class SecondEvent
    {
    }
}
