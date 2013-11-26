package net.md_5.bungee;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConnectionThrottle
{

    private final Map<InetAddress, Long> throttle = new HashMap<>();
    private final int throttleTime;

    public void unthrottle(InetAddress address)
    {
        throttle.remove( address );
    }

    public boolean throttle(InetAddress address)
    {
        Long value = throttle.get( address );
        long currentTime = System.currentTimeMillis();

        throttle.put( address, currentTime );
        return value != null && currentTime - value < throttleTime;
    }
}
