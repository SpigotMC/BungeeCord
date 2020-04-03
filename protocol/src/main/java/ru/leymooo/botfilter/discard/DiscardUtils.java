package ru.leymooo.botfilter.discard;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.protocol.Varint21FrameDecoder;

@UtilityClass
public class DiscardUtils
{

    private final boolean so_linger = Boolean.getBoolean( "useSoLinger" );

    public ChannelFuture discardAndClose(Channel channel)
    {
        ChannelPipeline pipeline = channel.pipeline();
        if ( pipeline.get( ChannelDiscardHandler.class ) == null )
        {
            pipeline.addAfter( "frame-decoder", ChannelDiscardHandler.DISCARD, ChannelDiscardHandler.HARD );
            pipeline.get( Varint21FrameDecoder.class ).shutdown();
            channel.config().setAutoRead( false );

            if ( so_linger )
            {
                channel.config().setOption( ChannelOption.SO_LINGER, 0 );
            }
        }
        return channel.close();
    }

    public void discard(Channel channel)
    {
        ChannelPipeline pipeline = channel.pipeline();
        if ( pipeline.get( ChannelDiscardHandler.class ) == null )
        {
            pipeline.addAfter( "frame-decoder", ChannelDiscardHandler.DISCARD, ChannelDiscardHandler.SOFT );
            pipeline.get( Varint21FrameDecoder.class ).shutdown();
        }
    }

}
