package net.md_5.bungee.api.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.event.EventBus;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.junit.jupiter.api.Test;

public class AsyncEventPriorityTest
{

    private final EventBus bus = new EventBus();
    private final CountDownLatch latch = new CountDownLatch( 8 );

    private final ExecutorService executor = Executors.newFixedThreadPool( 8 );

    @Test
    public void testPriority() throws InterruptedException
    {
        bus.register( this );
        bus.register( new AsyncEventPriorityListenerPartner() );
        CompletableCallback<AsyncPriorityTestEvent> callback = new CompletableCallback<>( (result, error) ->
        {
            assertEquals( 1, latch.getCount() );
            latch.countDown();
        } );
        bus.postAsync( new AsyncPriorityTestEvent( callback ) );

        synchronized ( callback )
        {
            callback.wait( 1000 );

            assertEquals( 0, latch.getCount() );

            executor.shutdown();
        }
    }

    @EventHandler(priority = Byte.MIN_VALUE)
    public void onMinPriority(AsyncPriorityTestEvent event)
    {
        event.registerIntent();

        executor.execute( () ->
        {
            assertEquals( 8, latch.getCount() );
            latch.countDown();

            event.completeIntent();
        } );
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLowestPriority(AsyncPriorityTestEvent event)
    {
        event.registerIntent();

        executor.execute( () ->
        {
            assertEquals( 7, latch.getCount() );
            latch.countDown();

            event.completeIntent();
        } );
    }

    @EventHandler
    public void onNormalPriority(AsyncPriorityTestEvent event)
    {
        event.registerIntent();

        executor.execute( () ->
        {
            assertEquals( 5, latch.getCount() );
            latch.countDown();

            event.completeIntent();
        } );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHighestPriority(AsyncPriorityTestEvent event)
    {
        event.registerIntent();

        executor.execute( () ->
        {
            assertEquals( 3, latch.getCount() );
            latch.countDown();

            event.completeIntent();
        } );
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onMaxPriority(AsyncPriorityTestEvent event)
    {
        event.registerIntent();

        executor.execute( () ->
        {
            assertEquals( 2, latch.getCount() );
            latch.countDown();

            event.completeIntent();
        } );
    }

    public static class AsyncPriorityTestEvent extends AsyncEvent<AsyncPriorityTestEvent>
    {
        public AsyncPriorityTestEvent(Callback<AsyncPriorityTestEvent> done)
        {
            super( done );
        }
    }

    public class AsyncEventPriorityListenerPartner
    {

        @EventHandler(priority = EventPriority.HIGH)
        public void onHighPriority(AsyncPriorityTestEvent event)
        {
            assertEquals( 4, latch.getCount() );
            latch.countDown();
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onLowPriority(AsyncPriorityTestEvent event)
        {
            assertEquals( 6, latch.getCount() );
            latch.countDown();
        }
    }

    @RequiredArgsConstructor
    private static class CompletableCallback<T> implements Callback<T>
    {

        private final Callback<T> callback;

        @Override
        public void done(T result, Throwable error)
        {
            this.callback.done( result, error );
            synchronized ( this )
            {
                notifyAll();
            }
        }
    }
}
