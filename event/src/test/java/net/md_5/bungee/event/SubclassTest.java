package net.md_5.bungee.event;

import java.util.concurrent.CountDownLatch;
import org.junit.Assert;
import org.junit.Test;

public class SubclassTest extends EventBusTest
{

    private final CountDownLatch latch = new CountDownLatch( 1 );

    @Test
    @Override
    public void testNestedEvents()
    {
        super.testNestedEvents();
        Assert.assertEquals( 0, latch.getCount() );
    }

    @EventHandler
    protected void extraListener(FirstEvent event)
    {
        Assert.assertEquals( 1, latch.getCount() );
        latch.countDown();
    }
}
