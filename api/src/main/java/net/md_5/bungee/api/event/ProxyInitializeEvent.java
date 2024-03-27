package net.md_5.bungee.api.event;

import java.util.Map;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Event;

public class ProxyInitializeEvent extends Event
{

    private final Map<ListenerInfo, Boolean> listenerState;

    public ProxyInitializeEvent(Map<ListenerInfo, Boolean> listenerState)
    {
        this.listenerState = listenerState;
    }

    public Map<ListenerInfo, Boolean> getListenerState()
    {
        return listenerState;
    }
}
