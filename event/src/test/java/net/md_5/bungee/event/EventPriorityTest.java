package net.md_5.bungee.event;

import net.md_5.bungee.api.plugin.Cancellable;
import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

public class EventPriorityTest
{

    private final EventBus bus = new EventBus();
    private final CountDownLatch latch = new CountDownLatch( 2 );
    private static final Logger logger = Logger.getGlobal();
    
    @Test
    public void testPriorityAndIgnoreCancelled()
    {
        bus.register( this );
        bus.post( new DummyCancellableEvent());
        Assert.assertEquals( 0, latch.getCount() );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void ignoreCancelTest(DummyCancellableEvent event)
    {
        logger.log( Level.INFO, MessageFormat.format( "Just logging here! Event is cancelled: {0}", event.isCancelled()));
        Assert.assertEquals( 1, latch.getCount() );
        latch.countDown();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void badWolfEvent(DummyCancellableEvent event) {
        //cancelled
        Assert.fail( "BAD WOLF EVENT");
    }
    
    @EventHandler
    public void doSomethingNormalPriority(DummyCancellableEvent event) {
        logger.log( Level.INFO, "I did something with HIGH priority" );
        Assert.assertEquals( 1, latch.getCount());
        event.setCancelled( true );
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void doSomethingLowPriority(DummyCancellableEvent event) {
        logger.log( Level.INFO, "I did something with low Priority!");
        Assert.assertEquals( 2, latch.getCount());
        latch.countDown();
    }

    public static class DummyCancellableEvent implements Cancellable
    {
        @Getter
        @Setter
        private boolean cancelled;
    }
}