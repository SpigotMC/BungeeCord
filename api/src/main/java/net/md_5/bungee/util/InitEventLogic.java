package net.md_5.bungee.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.event.ProxyInitializeEvent;


/**
 * This class contains the necessary code to fire the ProxyInitializeEvent, as soon as all listeners
 * have a defined state (port opened/port closed).
 */
public class InitEventLogic
{

    private static Map<ListenerInfo, Boolean> knownListenersState = new HashMap<>();

    private static ReentrantLock mapLock = new ReentrantLock();

    private static int amountOfListenersToWaitFor = -1;

    /**
     * Sets a given Listener via its {@link ListenerInfo} to a result. <br><br>
     * Thread-safe.
     * @param listenerInfo ListenerInfo to assign a state to.
     * @param success True: Port has opened. False: Port failed to open.
     * @return Object of ProxyInitializeEvent if this was the last listeners to be ready, otherwise null.
     */
    public static ProxyInitializeEvent setListenerAsInitialized(ListenerInfo listenerInfo, boolean success)
    {
        ProxyInitializeEvent result = null;

        if ( listenerInfo == null )
        {
            throw new NullPointerException( "ListenerInfo may not be null!" );
        }

        mapLock.lock();

        try {
            knownListenersState.put( listenerInfo, success );

            if(areAllListenersInitialized()) {
                result = new ProxyInitializeEvent( InitEventLogic.knownListenersState );
            }

        } finally {
            mapLock.unlock(); // Ensure map gets unlocked in case of error...
        }

        return result;
    }

    /**
     * Sets the amount of Listeners that are configured in the configuration of BungeeCord.
     * <br>
     * This can only be set <b>once</b>!
     * @param number Amount of Listeners. May not be negative!
     * @throws IllegalArgumentException Thrown if passed number is negative.
     * @throws IllegalStateException Thrown if function is called more than once.
     */
    public static void setNumberOfListenersToWaitFor(int number)
    {
        if ( number <= 0 )
        {
            throw new IllegalArgumentException( "The amount of listeners that have to be waited needs to be larger than zero." );
        }

        if ( InitEventLogic.amountOfListenersToWaitFor > 0 )
        {
            throw new IllegalStateException( "The amount of listeners cannot be set twice!" );
        }

        InitEventLogic.amountOfListenersToWaitFor = number;
    }

    /**
     * Checks if the amount of entries in the internal Map is equals to the previously set amount.
     * <br><br>
     * Requires a previous call to {@link InitEventLogic#setNumberOfListenersToWaitFor(int)}.<br>
     * <b>Important:</b>Will return false if the above-mentioned function was not called before,
     * even if entries were set via {@link InitEventLogic#setListenerAsInitialized(ListenerInfo, boolean)}
     * @return Have all Listeners a defined state?
     */
    public static boolean areAllListenersInitialized()
    {
        // Check if all listeners declared have initialized:

        if ( InitEventLogic.amountOfListenersToWaitFor == -1 )
        {
            return false;
        }

        return InitEventLogic.amountOfListenersToWaitFor == InitEventLogic.knownListenersState.size( );
    }

}
