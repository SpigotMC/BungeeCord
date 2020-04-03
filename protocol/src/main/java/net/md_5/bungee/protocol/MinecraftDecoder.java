package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.protocol.packet.Handshake;
import ru.leymooo.botfilter.discard.DiscardUtils;
import ru.leymooo.botfilter.discard.ErrorStream;

@AllArgsConstructor
public class MinecraftDecoder extends MessageToMessageDecoder<ByteBuf>
{

    @Setter
    private Protocol protocol;
    private final boolean server;
    @Setter
    private int protocolVersion;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        //BotFilter start
        int originalReaderIndex = in.readerIndex();
        int originalReadableBytes = in.readableBytes();
        int packetId = DefinedPacket.readVarInt( in );
        if ( packetId < 0 || packetId > Protocol.MAX_PACKET_ID )
        {
            DiscardUtils.discard( ctx.channel() ).addListener( (ChannelFutureListener) future ->
            {
                ErrorStream.error( "[" + ctx.channel().remoteAddress() + "] <-> MinecraftDecoder received invalid packet id " + packetId + ", disconnected" );
            } );
            return;
        }
        //BotFilter end
        Protocol.DirectionData prot = ( server ) ? protocol.TO_SERVER : protocol.TO_CLIENT;
        int protocolVersion = this.protocolVersion;
        DefinedPacket packet = prot.createPacket( packetId, protocolVersion );
        if ( packet != null )
        {
            //BotFilter start
            if ( packet instanceof Handshake )
            {
                try
                {
                    packet.read( in, prot.getDirection(), protocolVersion );
                } catch ( Exception e )
                {
                    DiscardUtils.discard( ctx.channel() ).addListener( (ChannelFutureListener) future ->
                    {
                        ErrorStream.error( "[" + ctx.channel().remoteAddress() + "] Sent wrong handshake" );
                    } );
                    return;
                }
            } else
            {
                //BotFilter end
                packet.read( in, prot.getDirection(), protocolVersion );
            }
            if ( in.isReadable() )
            {
                //BotFilter start
                in.skipBytes( in.readableBytes() ); //BotFilter end
                if ( server )
                {
                    DiscardUtils.discard( ctx.channel() ).addListener( (ChannelFutureListener) future ->
                    {
                        ErrorStream.error( "[" + ctx.channel().remoteAddress() + "] Longer than expected: Packet " + packetId + " Protocol " + protocol + " Direction " + prot.getDirection() );
                    } );
                    return;
                }
                throw new BadPacketException( "Did not read all bytes from packet " + packet.getClass() + " " + packetId + " Protocol " + protocol + " Direction " + prot.getDirection() );
            }
        } else
        {
            in.skipBytes( in.readableBytes() );
        }
        //System.out.println( "ID: " + packetId + ( packet == null ? " (null)" : " ("+packet+")" ) );
        ByteBuf copy = in.copy( originalReaderIndex, originalReadableBytes ); //BotFilter
        out.add( new PacketWrapper( packet, copy ) );
    }

    //BotFilter start
    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception
    {
        return msg instanceof ByteBuf;
    }
    //BotFilter end
}
