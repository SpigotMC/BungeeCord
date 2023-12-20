package net.md_5.bungee.event;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;

public class SubclassTest extends EventBusTest
{

    private final CountDownLatch latch = new CountDownLatch( 1 );

    @Test
    @Override
    public void testNestedEvents()
    {
        super.testNestedEvents();
        assertEquals( 0, latch.getCount() );
    }

    @EventHandler
    protected void extraListener(FirstEvent event)
    {
        assertEquals( 1, latch.getCount() );
        latch.countDown();
    }
}
