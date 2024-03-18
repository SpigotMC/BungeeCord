package net.md_5.bungee.event;

import java.lang.invoke.MethodHandles;

public class EventLookup
{

    static
    {
        EventBus.LOOKUPS.put( EventLookup.class.getClassLoader(), MethodHandles.lookup() );
    }
}
