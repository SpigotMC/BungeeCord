package net.md_5.bungee.event;

import java.util.concurrent.CountDownLatch;
import org.junit.Assert;
import org.junit.Test;

public class EventPriorityTest
{

    private final EventBus bus = new EventBus();
    private final CountDownLatch latch = new CountDownLatch( 5 );

    @Test
    public void testPriority()
    {
        bus.register( this );
        bus.register( new EventPriorityListenerPartner() );
        bus.post( new PriorityTestEvent() );
        Assert.assertEquals( 0, latch.getCount() );
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLowestPriority(PriorityTestEvent event)
    {
        Assert.assertEquals( 5, latch.getCount() );
        latch.countDown();
    }

    @EventHandler
    public void onNormalPriority(PriorityTestEvent event)
    {
        Assert.assertEquals( 3, latch.getCount() );
        latch.countDown();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHighestPriority(PriorityTestEvent event)
    {
        Assert.assertEquals( 1, latch.getCount() );
        latch.countDown();
    }

    public static class PriorityTestEvent
    {
    }

    public class EventPriorityListenerPartner
    {

        @EventHandler(priority = EventPriority.HIGH)
        public void onHighPriority(PriorityTestEvent event)
        {
            Assert.assertEquals( 2, latch.getCount() );
            latch.countDown();
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onLowPriority(PriorityTestEvent event)
        {
            Assert.assertEquals( 4, latch.getCount() );
            latch.countDown();
        }
    }
}
