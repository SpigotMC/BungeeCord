package net.md_5.bungee.api.event;

import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Event;

public class ProxyInitializeEvent extends Event
{

    private final ListenerInfo info;

    private final boolean success;


    public ProxyInitializeEvent( ListenerInfo info, boolean success )
    {

        if(info == null)
        {
            throw new NullPointerException( "ListenerInfo cannot be null!" );
        }

        this.info = info;
        this.success = success;
    }

    public ListenerInfo getInfo()
    {
        return info;
    }

    public boolean getSuccess()
    {
        return success;
    }
}
