package net.md_5.bungee.api.event;

import net.md_5.bungee.api.plugin.Event;

public class ProxyInitializeEvent extends Event
{

    private final boolean isListening;

    /**
     * Constructor
     * @param isListening Is Proxy Listening? (Did desired game-port open?)
     */
    public ProxyInitializeEvent(boolean isListening)
    {
        this.isListening = isListening;
    }

    public boolean isListening()
    {
        return this.isListening;
    }
}
