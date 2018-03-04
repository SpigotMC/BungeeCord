package net.md_5.bungee.netty;

import com.google.common.base.Preconditions;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.protocol.packet.Kick;

// SERVER_BOUND
public class DownstreamChannelWrapper extends ChannelWrapper
{

    public DownstreamChannelWrapper(ChannelHandlerContext ctx)
    {
        super( ctx );
    }

    public void closeChannel(Kick kick)
    {
        super.closeChannel();
    }

    public void close(final Kick kick)
    {
        // Use #close() instead.
        Preconditions.checkNotNull( kick, "Kick cannot be null" );

        if ( !closed ) // -> ch.isActive?
        {
            closed = closing = true;

            ch.writeAndFlush( kick ).addListeners( ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE, ChannelFutureListener.CLOSE );
            ch.eventLoop().schedule( new Runnable()
            {
                @Override
                public void run()
                {
                    closeChannel( kick );
                }
            }, 250, TimeUnit.MILLISECONDS );
        }
    }

    public void delayedClose(final Kick kick)
    {
        if ( !closing )
        {
            closing = true;

            // Minecraft client can take some time to switch protocols.
            // Sending the wrong disconnect packet whilst a protocol switch is in progress will crash it.
            // Delay 250ms to ensure that the protocol switch (if any) has definitely taken place.
            ch.eventLoop().schedule( new Runnable()
            {

                @Override
                public void run()
                {
                    close( kick );
                }
            }, 250, TimeUnit.MILLISECONDS );
        }
    }
}
