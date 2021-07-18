package net.md_5.bungee.event;

import java.util.concurrent.CountDownLatch;
import org.junit.Assert;
import org.junit.Test;

public class NonVoidReturnTypeTest
{

    private final EventBus bus = new EventBus();
    private final CountDownLatch latch = new CountDownLatch( 2 );

    @Test
    public void test()
    {
        bus.register( this );
        bus.post( new FirstEvent() );
        Assert.assertEquals( 0, latch.getCount() );
    }

    @EventHandler
    public FirstEvent firstListener(FirstEvent event)
    {
        bus.post( new SecondEvent() );
        Assert.assertEquals( 1, latch.getCount() );
        latch.countDown();
        return event;
    }

    @EventHandler
    public Object secondListener(SecondEvent event)
    {
        latch.countDown();
        return null;
    }

    public static class FirstEvent
    {
    }

    public static class SecondEvent
    {
    }
}
