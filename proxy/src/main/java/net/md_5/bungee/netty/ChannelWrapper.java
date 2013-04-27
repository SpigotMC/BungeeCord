package net.md_5.bungee.netty;

import io.netty.channel.Channel;

public class ChannelWrapper
{

    private final Channel ch;
    private final SillyPromise promise;

    public ChannelWrapper(Channel ch)
    {
        this.ch = ch;
        this.promise = new SillyPromise( ch );
    }

    public void write(Object packet)
    {
        ch.write( packet, promise );
    }

    public Channel getHandle()
    {
        return ch;
    }
}
