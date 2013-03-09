package net.md_5.bungee.netty;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.PacketHandler;

/**
 * This class is a primitive wrapper for {@link PacketHandler} instances tied to
 * channels to maintain simple states, and only call the required, adapted
 * methods when the channel is connected.
 */
public class HandlerBoss extends ChannelInboundMessageHandlerAdapter<ByteBuf>
{

    private PacketHandler handler;

    public void setHandler(PacketHandler handler)
    {
        Preconditions.checkArgument( handler != null, "handler" );
        this.handler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        if ( handler != null )
        {
            handler.connected( ctx.channel() );
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        if ( handler != null )
        {
            handler.disconnected( ctx.channel() );
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) throws Exception
    {
        if ( handler != null && ctx.channel().isActive() )
        {
            DefinedPacket packet = DefinedPacket.packet( msg );
            boolean sendPacket = true;
            if ( packet != null )
            {
                try
                {
                    packet.handle( handler );
                } catch ( CancelSendSignal ex )
                {
                    sendPacket = false;
                }
            }
            if ( sendPacket )
            {
                handler.handle( msg );
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        cause.printStackTrace();
        if ( ctx.channel().isActive() )
        {
            ctx.close();
        }
    }
}
