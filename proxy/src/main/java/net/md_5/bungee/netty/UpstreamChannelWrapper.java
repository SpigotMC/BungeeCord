package net.md_5.bungee.netty;

import io.netty.channel.ChannelHandlerContext;

// CLIENT_BOUND
public class UpstreamChannelWrapper extends ChannelWrapper
{

    public UpstreamChannelWrapper(ChannelHandlerContext ctx)
    {
        super( ctx );
    }
}
