package net.md_5.bungee.api.plugin;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.util.concurrent.CountDownLatch;
import org.junit.Assert;
import org.junit.Test;

public class EventBusTest
{

    private final EventBus bus = new EventBus();
    private final CountDownLatch latch = new CountDownLatch( 1 );

    @Test
    public void testNestedEvents()
    {
        bus.register( this );
        bus.post( new FirstEvent() );
    }

    @Subscribe
    public void firstListener(FirstEvent event)
    {
        bus.post( new SecondEvent() );
        Assert.assertEquals( latch.getCount(), 0 );
    }

    @Subscribe
    public void secondListener(SecondEvent event)
    {
        latch.countDown();
    }

    public static class FirstEvent extends Event
    {
    }

    public static class SecondEvent extends Event
    {
    }
}
