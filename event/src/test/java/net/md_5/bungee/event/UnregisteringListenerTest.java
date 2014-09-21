package net.md_5.bungee.event;

import org.junit.Assert;
import org.junit.Test;

public class UnregisteringListenerTest extends ImplementationRegistry
{

    public UnregisteringListenerTest(EventBus bus)
    {
        super( bus );
    }

    @Test
    public void testPriority()
    {
        bus.register( this );
        bus.unregister( this );
        bus.post( new TestEvent() );
    }

    @EventHandler
    public void onEvent(TestEvent evt)
    {
        Assert.fail( "Event listener wasn't unregistered" );
    }

    public static class TestEvent
    {
    }
}
