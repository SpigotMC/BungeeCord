package net.md_5.bungee.util;

import io.netty.channel.Channel;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.md_5.bungee.error.Errors;

@UtilityClass
public class ChannelUtil
{
    private final String DISCARD_HANDLER = "discard-handler";

    @SneakyThrows
    public void shutdownChannel(Channel channel, Throwable t)
    {
        val pipeline = channel.pipeline();
        if ( pipeline.get( DISCARD_HANDLER ) == null )
        {
            channel.config().setAutoRead( false );
            pipeline.addFirst( DISCARD_HANDLER, ChannelDiscardHandler.INSTANCE );
            channel.close();
            if ( Errors.isDebug() && t != null )
            {
                throw t;
            }
        }
    }
}
