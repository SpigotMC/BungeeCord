package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.LoginRequest;
import ru.leymooo.botfilter.utils.FastCorruptedFrameException;
import ru.leymooo.botfilter.utils.FastOverflowPacketException;

public class Varint21FrameDecoder extends ByteToMessageDecoder
{
    //BotFilter start
    private boolean fromBackend;
    //see https://github.com/PaperMC/Waterfall/pull/609/
    private int packetCount;

    public void setFromBackend(boolean fromBackend)
    {
        this.fromBackend = fromBackend;
    }

    //BotFilter end
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        //BotFilter start - rewrite Varint21Decoder
        if ( !ctx.channel().isActive() ) //BotFilter - added this if statament
        {
            in.skipBytes( in.readableBytes() );
            return;
        }

        int origReaderIndex = in.readerIndex();

        int i = 3;
        while ( i-- > 0 )
        {
            if ( !in.isReadable() )
            {
                in.readerIndex( origReaderIndex );
                return;
            }

            byte read = in.readByte();
            if ( read >= 0 )
            {
                // Make sure reader index of length buffer is returned to the beginning
                in.readerIndex( origReaderIndex );
                int packetLength = DefinedPacket.readVarInt( in );

                if ( packetLength <= 0 && !fromBackend ) // BotFilter dont throw exception for empties packets from backend
                {
                    super.setSingleDecode( true );  // BotFilter
                    throw new FastCorruptedFrameException( "Empty Packet!" );
                }

                if ( !fromBackend && packetCount < 4 )
                {
                    checkPacketLength( packetLength );
                }

                if ( in.readableBytes() < packetLength )
                {
                    in.readerIndex( origReaderIndex );
                    return;
                }
                out.add( in.readRetainedSlice( packetLength ) );
                return;
            }
        }

        super.setSingleDecode( true ); // BotFilter
        throw new FastCorruptedFrameException( "length wider than 21-bit" ); // BotFilter
    }

    private void checkPacketLength(int length)
    {
        int maxLength = 2097151; // max length of 21-bit varint

        switch ( packetCount )
        {
            case 0:
                maxLength = Handshake.EXPECTED_MAX_LENGTH + 2;
                break;
            case 1:
                // in case of server list ping, the the packets we get after handshake are always smaller
                // than any of these, so no need for special casing
                maxLength = LoginRequest.EXPECTED_MAX_LENGTH + 1;
                break;
            case 2:
            case 3:
                //For 2:
                // if offline mode we get minecraft:brand (bigger), otherwise we get EncryptionResponse
                // so we check for the bigger packet, we are still far below critical maximum sizes
                // minecraft:brand (16 bytes) followed by a 400 char long string should never be reached
                //For 3:
                // if offline mode we get either teleport confirm or player pos&look
                // otherwise we get minecraft:brand (bigger max size)
                maxLength = 16 + ( 400 * 4 + 3 );
                break;
        }
        if ( length > maxLength )
        {
            throw new FastOverflowPacketException( "Packet #" + packetCount + " could not be framed because was too large"
                + " (expected " + maxLength + " bytes, got " + length + " bytes)" );
        }
        packetCount++;
    }
    //BotFilter end
}
