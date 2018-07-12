package net.md_5.bungee.query;

import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.QueryResponse;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.event.ProxyQueryEvent;

@RequiredArgsConstructor
public class QueryHandler extends SimpleChannelInboundHandler<DatagramPacket>
{

    private final ProxyServer bungee;
    private final ListenerInfo listener;
    /*========================================================================*/
    private final Random random = new Random();
    private final Cache<InetAddress, QuerySession> sessions = CacheBuilder.newBuilder().expireAfterWrite( 30, TimeUnit.SECONDS ).build();

    private void writeShort(ByteBuf buf, int s)
    {
        buf.writeShortLE( s );
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
            sessions.put( msg.sender().getAddress(), new QuerySession( challengeToken, System.currentTimeMillis() ) );

            writeNumber( out, challengeToken );
        }

        if ( type == 0x00 )
        {
            int challengeToken = in.readInt();
            QuerySession session = sessions.getIfPresent( msg.sender().getAddress() );
            if ( session == null || session.getToken() != challengeToken )
            {
                throw new IllegalStateException( "No session!" );
            }

            out.writeByte( 0x00 );
            out.writeInt( sessionId );

            boolean longResponse;
            if ( in.readableBytes() == 0 )
            {
                longResponse = false;
            } else if ( in.readableBytes() == 4 )
            {
                longResponse = true;
            } else
            {
                // Error!
                throw new IllegalStateException( "Invalid data request packet" );
            }

            QueryResponse query = bungee.getPluginManager().callEvent( new ProxyQueryEvent( longResponse, listener, new QueryResponse( bungee, listener ) ) ).getResponse();

            if ( !longResponse )
            {
                // Short response
                writeString( out, query.getMotd() ); // MOTD
                writeString( out, "SMP" ); // Game Type
                writeString( out, query.getWorld() ); // World Name
                writeNumber( out, query.getPlayerCount() ); // Online Count
                writeNumber( out, query.getMaxPlayers() ); // Max Players
                writeShort( out, query.getPort() ); // Port
                writeString( out, query.getAddress() ); // IP
            } else
            {
                // Long Response
                out.writeBytes( new byte[]
                {
                    0x73, 0x70, 0x6C, 0x69, 0x74, 0x6E, 0x75, 0x6D, 0x00, (byte) 0x80, 0x00
                } );
                Map<String, String> data = new LinkedHashMap<>();

                data.put( "hostname", query.getMotd() );
                data.put( "gametype", "SMP" );
                // Start Extra Info
                data.put( "game_id", "MINECRAFT" );
                data.put( "version", query.getVersion() );
                data.put( "plugins", query.getPlugins().isEmpty() ? "" : query.getServer() + ": " + Joiner.on( "; " ).join( query.getPlugins() ) );
                // End Extra Info
                data.put( "map", query.getWorld() );
                data.put( "numplayers", Integer.toString( query.getPlayerCount() ) );
                data.put( "maxplayers", Integer.toString( query.getMaxPlayers() ) );
                data.put( "hostport", Integer.toString( query.getPort() ) );
                data.put( "hostip", query.getAddress() );

                for ( Map.Entry<String, String> entry : data.entrySet() )
                {
                    writeString( out, entry.getKey() );
                    writeString( out, entry.getValue() );
                }
                out.writeByte( 0x00 ); // Null

                // Padding
                writeString( out, "\01player_\00" );
                // Player List
                for ( String s : query.getPlayers() )
                {
                    writeString( out, s );
                }
                out.writeByte( 0x00 ); // Null
            }
        }

        ctx.writeAndFlush( response );
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        bungee.getLogger().log( Level.WARNING, "Error whilst handling query packet from " + ctx.channel().remoteAddress(), cause );
    }

    @Data
    private static class QuerySession
    {

        private final int token;
        private final long time;
    }
}
