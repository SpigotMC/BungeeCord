package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.protocol.skip.PacketReader;

/**
 * This class will attempt to read a packet from {@link PacketReader}, with the
 * specified {@link #protocol} before returning a new {@link ByteBuf} with the
 * copied contents of all bytes read in this frame.
 * <p/>
 * It is based on {@link ReplayingDecoder} so that packets will only be returned
 * when all needed data is present.
 */
@AllArgsConstructor
public class PacketDecoder extends ReplayingDecoder<Void>
{

    @Getter
    @Setter
    private int protocol;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, MessageBuf<Object> out) throws Exception
    {
        // While we have enough data
        while ( true )
        {
            // Store our start index
            int startIndex = in.readerIndex();
            // Run packet through framer
            PacketReader.readPacket( in, protocol );
            // If we got this far, it means we have formed a packet, so lets grab the end index
            int endIndex = in.readerIndex();
            // Allocate a buffer big enough for all bytes we have read
            byte[] buf = new byte[ endIndex - startIndex ];
            // Go back to start index
            in.readerIndex( startIndex );
            // Drain all the bytes into our buffer
            in.readBytes( buf, 0, buf.length );
            // Jump back to the end of this packet
            in.readerIndex( endIndex );
            // Checkpoint our state incase we don't have enough data for another packet
            checkpoint();
            // Store our decoded message
            out.add( buf );
        }
    }
}
