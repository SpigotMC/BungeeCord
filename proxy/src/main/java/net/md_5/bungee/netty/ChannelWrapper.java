package net.md_5.bungee.netty;

import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelWrapper
{

    private final Channel ch;

    public void write(Object packet)
    {
        ch.write( packet );
    }

    public Channel getHandle()
    {
        return ch;
    }
}
