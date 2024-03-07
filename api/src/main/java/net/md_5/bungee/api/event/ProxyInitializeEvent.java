package net.md_5.bungee.api.event;

import jdk.vm.ci.meta.TriState;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyInitializeEvent extends Event
{

    private static int amountOfListenersToWaitFor = 0;
    private static Map<ListenerInfo, TriState> listenersToWaitFor = new ConcurrentHashMap<>();

    public static void setListenerAsInitialized(ListenerInfo listenerInfo, boolean success) {
        if(listenerInfo == null) {
            throw new NullPointerException("ListenerInfo may not be null!");
        }

        listenersToWaitFor.put(listenerInfo, (success ? TriState.TRUE : TriState.FALSE));
    }

    public static void setNumberOfListenersToWaitFor(int number) {
        if(number <= 0) {
            throw new IllegalArgumentException("The amount of listeners that have to be waited needs to be larger than zero.");
        }

        if(ProxyInitializeEvent.amountOfListenersToWaitFor > 0) {
            throw new IllegalStateException("The amount of listeners cannot be set twice!");
        }

        ProxyInitializeEvent.amountOfListenersToWaitFor = number;
    }


    public boolean areAllInitialized() {

        // Check if all listeners declared have initialized:

        if(amountOfListenersToWaitFor == 0) {
            return false;
        }

        return ProxyInitializeEvent.amountOfListenersToWaitFor == ProxyInitializeEvent.listenersToWaitFor.size();

    }

    public Map<ListenerInfo, TriState> getInitializationState() {
        return new ConcurrentHashMap<>(ProxyInitializeEvent.listenersToWaitFor);
    }

}
