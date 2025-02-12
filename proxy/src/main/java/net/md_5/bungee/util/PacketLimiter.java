package net.md_5.bungee.util;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PacketLimiter
{
    // max amount of packets allowed per second
    private final int limit;
    // max amount of data allowed per second
    private final int dataLimit;

    private int counter;
    private int dataCounter;
    private long nextSecond;

    public void received(int size)
    {
        counter++;
        dataCounter += size;

        if ( ( limit > 0 && counter > limit ) || ( dataLimit > 0 && dataCounter > dataLimit ) )
        {
            long now = System.currentTimeMillis();
            if ( nextSecond > now )
            {
                throw new QuietException( "exceeded packet limit" );
            }
            nextSecond = now + 1000;
            counter = 0;
            dataLimit = 0;
        }
    }
}
