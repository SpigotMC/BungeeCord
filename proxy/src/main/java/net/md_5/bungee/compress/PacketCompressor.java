package net.md_5.bungee.compress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;
import lombok.Setter;
import net.md_5.bungee.jni.zlib.BungeeZlib;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;

public class PacketCompressor extends MessageToByteEncoder<Object>
{

    private final BungeeZlib zlib = CompressFactory.zlib.newInstance();
    @Setter
    private int threshold = 256;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception
    {
        zlib.init( true, Deflater.DEFAULT_COMPRESSION );
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        zlib.free();
    }

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception
    {
        return msg instanceof ByteBuf || msg instanceof PacketWrapper;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msgObj, ByteBuf out) throws Exception
    {
        boolean isByteBuf = msgObj instanceof ByteBuf;
        if ( isByteBuf )
        {
            ByteBuf msg = (ByteBuf) msgObj;
            int origSize = msg.readableBytes();
            if ( origSize < threshold )
            {
                DefinedPacket.writeVarInt( 0, out );
                out.writeBytes( msg );
            } else
            {
                DefinedPacket.writeVarInt( origSize, out );

                zlib.process( msg, out );
            }
        } else
        {
            PacketWrapper wrapper = (PacketWrapper) msgObj;
            ByteBuf msg = wrapper.buf;
            try
            {
                int origSize = msg.readableBytes();
                if ( origSize < threshold )
                {
                    DefinedPacket.writeVarInt( 0, out );
                    out.writeBytes( msg );
                } else
                {
                    DefinedPacket.writeVarInt( origSize, out );

                    if ( wrapper.packet == null && wrapper.compressed != null )
                    {
                        out.writeBytes( wrapper.compressed );
                    } else
                    {
                        zlib.process( msg, out );
                    }
                }
            } finally
            {
                msg.release();
                wrapper.destroyCompressed();
            }
        }
    }
}
