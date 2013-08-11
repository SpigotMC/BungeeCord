/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.md_5.bungee.event;

import org.junit.Test;

/**
 *
 * @author Alex
 */
public class EventPriorityTest
{
    private final EventBus bus = new EventBus();

    @Test
    public void testEventPriorities()
    {
        bus.register( this );
        bus.post( new FirstEvent() );
    }


    
    @EventHandler(priority=EventPriority.LOWEST)
    public void firstListenerLOWEST(FirstEvent event)
    {
        System.out.println("This is the lowest event priority.");
        event.setCancelled(true);
    }

    @EventHandler(priority=EventPriority.LOW,ignoreCancelled=true)
    public void firstListenerLOW(FirstEvent event)
    {
        System.out.println("This is the low event priority. This should NOT be displayed!");
    }
    
    @EventHandler
    public void firstListener(FirstEvent event)
    {
        System.out.println("This is the normal/not-set event priority.");
        event.setCancelled(false);
    }

    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=true)
    public void firstListenerHIGH(FirstEvent event)
    {
        System.out.println("This is the high event priority. This should be displayed!");
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void firstListenerHIGHEST(FirstEvent event)
    {
        System.out.println("This is the highest event priority.");
    }
    
    @EventHandler(priority=EventPriority.MONITOR)
    public void firstListenerMONITOR(FirstEvent event)
    {
        System.out.println("This is the monitor event priority.");
    }

    public static class FirstEvent implements Cancellable
    {
        private boolean cancelled=true;
        @Override
        public boolean isCancelled()
        {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            cancelled=cancel;
        }
    }
}
