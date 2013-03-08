package net.md_5.bungee.netty;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.PacketHandler;

public class HandlerBoss extends ChannelInboundMessageHandlerAdapter<ByteBuf>
{

    private PacketHandler handler;

    HandlerBoss(PacketHandler handler)
    {
        Preconditions.checkArgument( handler != null, "handler" );
        this.handler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        handler.connected( ctx.channel() );
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        handler.disconnected( ctx.channel() );
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) throws Exception
    {
        if ( ctx.channel().isActive() )
        {
            DefinedPacket packet = DefinedPacket.packet( msg );
            if ( packet != null )
            {
                packet.handle( handler );
            } else
            {
                handler.handle( msg );
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        if ( ctx.channel().isActive() )
        {
            ctx.close();
        }
    }
}
