package net.md_5.bungee;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.PacketCDClientStatus;
import net.md_5.bungee.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketHandler;
import net.md_5.bungee.packet.PacketStream;

public class ServerConnector extends PacketHandler
{

    private final PacketStream stream;
    private Packet1Login loginPacket;
    private State thisState = State.ENCRYPT_REQUEST;

    public ServerConnector(PacketStream stream)
    {
        this.stream = stream;
    }

    private enum State
    {

        ENCRYPT_REQUEST, LOGIN, FINISHED;
    }

    @Override
    public void handle(Packet1Login login) throws Exception
    {
        Preconditions.checkState( thisState == State.LOGIN, "Not exepcting LOGIN" );
        loginPacket = login;
        thisState = State.FINISHED;
    }

    @Override
    public void handle(PacketFDEncryptionRequest encryptRequest) throws Exception
    {
        Preconditions.checkState( thisState == State.ENCRYPT_REQUEST, "Not expecting ENCRYPT_REQUEST" );
        thisState = State.LOGIN;
    }

    @Override
    public void handle(PacketFFKick kick) throws Exception
    {
        throw new KickException( kick.message );
    }

    public static ServerConnection connect(UserConnection user, ServerInfo info, boolean retry)
    {
        Socket socket = null;
        try
        {
            socket = new Socket();
            socket.connect( info.getAddress(), BungeeCord.getInstance().config.getTimeout() );
            BungeeCord.getInstance().setSocketOptions( socket );
            PacketStream stream = new PacketStream( socket.getInputStream(), socket.getOutputStream(), user.stream.getProtocol() );

            ServerConnector connector = new ServerConnector( stream );
            stream.write( user.handshake );
            stream.write( PacketCDClientStatus.CLIENT_LOGIN );

            while ( connector.thisState != State.FINISHED )
            {
                byte[] buf = stream.readPacket();
                DefinedPacket packet = DefinedPacket.packet( buf );
                packet.handle( connector );
            }

            ServerConnection server = new ServerConnection( socket, info, stream, connector.loginPacket );
            ServerConnectedEvent event = new ServerConnectedEvent( user, server );
            ProxyServer.getInstance().getPluginManager().callEvent( event );

            stream.write( BungeeCord.getInstance().registerChannels() );

            Queue<DefinedPacket> packetQueue = ( (BungeeServerInfo) info ).getPacketQueue();
            while ( !packetQueue.isEmpty() )
            {
                stream.write( packetQueue.poll() );
            }
            return server;
        } catch ( Exception ex )
        {
            if ( socket != null )
            {
                try
                {
                    socket.close();
                } catch ( IOException ioe )
                {
                }
            }
            ServerInfo def = ProxyServer.getInstance().getServers().get( user.getPendingConnection().getListener().getDefaultServer() );
            if ( retry && !info.equals( def ) )
            {
                user.sendMessage( ChatColor.RED + "Could not connect to target server, you have been moved to the default server" );
                return connect( user, def, false );
            } else
            {
                if ( ex instanceof RuntimeException )
                {
                    throw (RuntimeException) ex;
                }
                throw new RuntimeException( "Could not connect to target server " + Util.exception( ex ) );
            }
        }
    }
}
