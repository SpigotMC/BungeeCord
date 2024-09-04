package net.md_5.bungee.compress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import java.util.zip.Deflater;
import lombok.Setter;
import net.md_5.bungee.jni.cipher.JavaCipher;
import net.md_5.bungee.jni.zlib.BungeeZlib;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.DefinedPacket;

public class PacketCompressor extends MessageToMessageEncoder<ByteBuf>
{

    private final BungeeZlib zlib = CompressFactory.zlib.newInstance();
    @Setter
    private int threshold = 256;
    private boolean fast;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception
    {
        CipherEncoder cipher = ctx.pipeline().get( CipherEncoder.class );
        fast = cipher == null || cipher.getCipher() instanceof JavaCipher;
        zlib.init( true, Deflater.DEFAULT_COMPRESSION );
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        zlib.free();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception
    {
        int origSize = msg.readableBytes();
        if ( origSize < threshold )
        {
            if ( fast )
            {
                // create a virtual buffer to avoid copying of data
                out.add( ctx.alloc().compositeBuffer( 2 ).addComponents( true, ctx.alloc().directBuffer( 1 ).writeByte( 0 ), msg.retain() ) );
            } else
            {
                out.add( ctx.alloc().directBuffer( origSize + 1 ).writeByte( 0 ).writeBytes( msg ) );
            }
        } else
        {
            // native impl ensureWritable( 8192 )
            ByteBuf buf = ctx.alloc().directBuffer( 8192 );
            DefinedPacket.writeVarInt( origSize, buf );
            zlib.process( msg, buf );
            out.add( buf );
        }
    }
}
