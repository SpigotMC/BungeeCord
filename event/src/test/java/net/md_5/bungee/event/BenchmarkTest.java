package net.md_5.bungee.event;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class BenchmarkTest extends ImplementationRegistry
{

    private final AtomicInteger executionCount = new AtomicInteger();

    public BenchmarkTest(EventBus bus)
    {
        super( bus );
    }

    @Test
    public void bench()
    {
        bus.register( this );

        Object firstEvent = new EventBusTest.FirstEvent();
        Object secondEvent = new EventBusTest.SecondEvent();

        long elapsed = 0;

        for ( int i = 0; i < 10; i++ )
        {
            long start = System.nanoTime();
            for ( int j = 0; j < 2500000; j++ ) // 2.5 million
            {
                bus.post( ( j % 2 == 0 ) ? firstEvent : secondEvent );
            }
            elapsed = ( System.nanoTime() - start );

        }
        System.out.println( "Elapsed Time for 10th run of " + bus + ": " + elapsed + "ns (" + TimeUnit.NANOSECONDS.toMillis( elapsed ) + "ms)" );

    }

    @EventHandler
    public void firstListener(EventBusTest.FirstEvent event)
    {
        executionCount.incrementAndGet();
    }

    @EventHandler
    public void secondListener(EventBusTest.SecondEvent event)
    {
        executionCount.incrementAndGet();
    }
}
