package net.md_5.bungee.netty;

import io.netty.channel.Channel;

public class ChannelWrapper
{

    private final Channel ch;

    public ChannelWrapper(Channel ch)
    {
        this.ch = ch;
    }

    public void write(Object packet)
    {
        ch.write( packet, ch.voidPromise() );
    }

    public Channel getHandle()
    {
        return ch;
    }
}
