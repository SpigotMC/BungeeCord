package net.md_5.bungee.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PacketLimiter
{
    // max amount of packets allowed per second
    @Getter
    private final int limit;
    private int counter;
    private long nextSecond;

    public void received()
    {
        if ( ++counter == limit )
        {
            long now = System.currentTimeMillis();
            if ( nextSecond > now )
            {
                throw new QuietException( "exceeded packet limit" );
            }
            nextSecond = now + 1000;
            counter = 0;
        }
    }
}
