package net.md_5.bungee.event;

import java.util.concurrent.CountDownLatch;
import org.junit.Assert;
import org.junit.Test;

public class EventBusTest
{

    private final EventBus bus = new EventBus();
    private final CountDownLatch latch = new CountDownLatch( 2 );

    @Test
    public void testNestedEvents()
    {
        bus.register( this );
        bus.post( new FirstEvent() );
        Assert.assertEquals( latch.getCount(), 0 );
    }

    @EventHandler
    public void firstListener(FirstEvent event)
    {
        bus.post( new SecondEvent() );
        Assert.assertEquals( latch.getCount(), 1 );
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
