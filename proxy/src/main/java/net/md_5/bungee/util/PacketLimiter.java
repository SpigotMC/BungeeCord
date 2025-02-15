package net.md_5.bungee.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PacketLimiter
{

    // max amount of packets allowed per second
    private final int limit;
    // max amount of data allowed per second
    private final int dataLimit;

    @Getter
    private int counter;
    @Getter
    private int dataCounter;
    private long nextSecond;

    /**
     * Counts the received packet amount and size.
     *
     * @param size size of the packet
     * @return return false if the player should be kicked
     */
    public boolean incrementAndCheck(int size)
    {
        counter++;
        dataCounter += size;

        if ( ( limit > 0 && counter > limit ) || ( dataLimit > 0 && dataCounter > dataLimit ) )
        {
            long now = System.currentTimeMillis();
            if ( nextSecond > now )
            {
                return false;
            }
            nextSecond = now + 1000;
            counter = 0;
            dataCounter = 0;
        }
        return true;
    }
}
