package net.md_5.bungee.query;

import io.netty.buffer.ByteBuf;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@RequiredArgsConstructor
public class QueryHandler extends SimpleChannelInboundHandler<DatagramPacket>
{

    private final ProxyServer bungee;
    private final ListenerInfo listener;
    /*========================================================================*/
    private final Random random = new Random();
    private final Map<Integer, Long> sessions = new HashMap<>();

    private void writeShort(ByteBuf buf, int s)
    {
        buf.order( ByteOrder.LITTLE_ENDIAN ).writeShort( s );
    }

    private void writeNumber(ByteBuf buf, int i)
    {
        writeString( buf, Integer.toString( i ) );
    }

    private void writeString(ByteBuf buf, String s)
    {
        for ( char c : s.toCharArray() )
        {
            buf.writeByte( c );
        }
        buf.writeByte( 0x00 );
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception
    {
        ByteBuf in = msg.content();
        if ( in.readUnsignedByte() != 0xFE || in.readUnsignedByte() != 0xFD )
        {
            bungee.getLogger().log( Level.WARNING, "Query - Incorrect magic!: {0}", msg.sender() );
            return;
        }

        ByteBuf out = ctx.alloc().buffer();
        AddressedEnvelope response = new DatagramPacket( out, msg.sender() );

        byte type = in.readByte();
        int sessionId = in.readInt();

        if ( type == 0x09 )
        {
            out.writeByte( 0x09 );
            out.writeInt( sessionId );

            int challengeToken = random.nextInt();
            sessions.put( challengeToken, System.currentTimeMillis() );

            writeNumber( out, challengeToken );
        }

        if ( type == 0x00 )
        {
            int challengeToken = in.readInt();
            Long session = sessions.get( challengeToken );
            if ( session == null || System.currentTimeMillis() - session > TimeUnit.SECONDS.toMillis( 30 ) )
            {
                throw new IllegalStateException( "No session!" );
            }

            out.writeByte( 0x00 );
            out.writeInt( sessionId );

            if ( in.readableBytes() == 0 )
            {
                // Short response
                writeString( out, listener.getMotd() ); // MOTD
                writeString( out, "SMP" ); // Game Type
                writeString( out, "BungeeCord_Proxy" ); // World Name
                writeNumber( out, bungee.getOnlineCount() ); // Online Count
                writeNumber( out, listener.getMaxPlayers() ); // Max Players
                writeShort( out, listener.getHost().getPort() ); // Port
                writeString( out, listener.getHost().getHostString() ); // IP
            } else if ( in.readableBytes() == 4 )
            {
                // Long Response
                out.writeBytes( new byte[]
                {
                    0x73, 0x70, 0x6C, 0x69, 0x74, 0x6E, 0x75, 0x6D, 0x00, (byte) 0x80, 0x00
                } );
                Map<String, String> data = new LinkedHashMap<>();

                data.put( "hostname", listener.getMotd() );
                data.put( "gametype", "SMP" );
                // Start Extra Info
                data.put( "game_id", "MINECRAFT" );
                data.put( "version", bungee.getGameVersion() );
                data.put( "plugins", "" );
                // End Extra Info
                data.put( "map", "BungeeCord_Proxy" );
                data.put( "numplayers", Integer.toString( bungee.getOnlineCount() ) );
                data.put( "maxplayers", Integer.toString( listener.getMaxPlayers() ) );
                data.put( "hostport", Integer.toString( listener.getHost().getPort() ) );
                data.put( "hostip", listener.getHost().getHostString() );

                for ( Map.Entry<String, String> entry : data.entrySet() )
                {
                    writeString( out, entry.getKey() );
                    writeString( out, entry.getValue() );
                }
                out.writeByte( 0x00 ); // Null                

                // Padding
                writeString( out, "\01player_\00" );
                // Player List
                for ( ProxiedPlayer p : bungee.getPlayers() )
                {
                    writeString( out, p.getName() );
                }
                out.writeByte( 0x00 ); // Null
            } else
            {
                // Error!
                throw new IllegalStateException( "Invalid data request packet" );
            }
        }

        ctx.writeAndFlush( response );
    }
}
