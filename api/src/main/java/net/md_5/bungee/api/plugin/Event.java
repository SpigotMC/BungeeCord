package net.md_5.bungee.api.plugin;

/**
 * Dummy class which all callable events must extend.
 */
public abstract class Event
{

    /**
     * Method called after this event has been dispatched to all handlers.
     */
    public void postCall()
    {
    }
}
