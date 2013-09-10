package net.md_5.bungee;

import gnu.trove.map.hash.TObjectLongHashMap;
import java.net.InetAddress;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConnectionThrottle
{

    private final TObjectLongHashMap<InetAddress> throttle = new TObjectLongHashMap<>();
    private final int throttleTime;

    public void unthrottle(InetAddress address)
    {
        throttle.remove( address );
    }

    public boolean throttle(InetAddress address)
    {
        long value = throttle.get( address );
        long currentTime = System.currentTimeMillis();

        throttle.put( address, currentTime );
        return value != 0 && currentTime - value < throttleTime;
    }
}
