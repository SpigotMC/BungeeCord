package ru.leymooo.botfilter.discard;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class DiscardUtils
{
    public ChannelFuture discard(Channel channel)
    {
        val pipeline = channel.pipeline();
        if ( pipeline.get( ChannelDiscardHandler.class ) == null )
        {
            pipeline.addFirst( ChannelDiscardHandler.DISCARD, ChannelDiscardHandler.INSTANCE );
            channel.config().setAutoRead( false );
        }
        return channel.close();
    }

}
