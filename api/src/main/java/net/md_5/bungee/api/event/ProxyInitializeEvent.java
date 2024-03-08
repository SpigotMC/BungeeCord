package net.md_5.bungee.api.event;

import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyInitializeEvent extends Event
{

    private final Map<ListenerInfo, Boolean> listenerState;

    public ProxyInitializeEvent(Map<ListenerInfo, Boolean> listenerState) {
        this.listenerState = listenerState;
    }

    public Map<ListenerInfo, Boolean> getListenerState() {
        return listenerState;
    }
}
