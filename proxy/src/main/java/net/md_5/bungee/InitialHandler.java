package net.md_5.bungee;

import com.google.common.base.Preconditions;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.PacketCDClientStatus;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.packet.PacketFEPing;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketHandler;
import net.md_5.bungee.packet.PacketStream;
import net.md_5.bungee.protocol.PacketDefinitions;

public class InitialHandler extends PacketHandler implements Runnable, PendingConnection
{

    private final Socket socket;
    @Getter
    private final ListenerInfo listener;
    private PacketStream stream;
    private Packet1Login forgeLogin;
    private Packet2Handshake handshake;
    private PacketFDEncryptionRequest request;
    private List<PacketFAPluginMessage> loginMessages = new ArrayList<>();
    private State thisState = State.HANDSHAKE;
    private static final PacketFAPluginMessage forgeMods = new PacketFAPluginMessage( "FML", new byte[]
    {
        0, 0, 0, 0, 0, 2
    } );

    public InitialHandler(Socket socket, ListenerInfo info) throws IOException
    {
        this.socket = socket;
        this.listener = info;
        stream = new PacketStream( socket.getInputStream(), socket.getOutputStream(), PacketDefinitions.VANILLA_PROTOCOL );
    }

    private enum State
    {

        HANDSHAKE, ENCRYPT, LOGIN, FINISHED;
    }

    @Override
    public void handle(Packet1Login login) throws Exception
    {
        Preconditions.checkState( thisState == State.LOGIN, "Not expecting FORGE LOGIN" );
        Preconditions.checkState( forgeLogin == null, "Already received FORGE LOGIN" );
        forgeLogin = login;
        stream.setProtocol( PacketDefinitions.FORGE_PROTOCOL );
    }

    @Override
    public void handle(PacketFAPluginMessage pluginMessage) throws Exception
    {
        loginMessages.add( pluginMessage );
    }

    @Override
    public void handle(PacketFEPing ping) throws Exception
    {
        socket.setSoTimeout( 100 );
        boolean newPing = false;
        try
        {
            socket.getInputStream().read();
            newPing = true;
        } catch ( IOException ex )
        {
        }

        ServerPing pingevent = new ServerPing( BungeeCord.PROTOCOL_VERSION, BungeeCord.GAME_VERSION,
                listener.getMotd(), ProxyServer.getInstance().getPlayers().size(), listener.getMaxPlayers() );

        pingevent = ProxyServer.getInstance().getPluginManager().callEvent( new ProxyPingEvent( this, pingevent ) ).getResponse();

        String response = ( newPing ) ? ChatColor.COLOR_CHAR + "1"
                + "\00" + pingevent.getProtocolVersion()
                + "\00" + pingevent.getGameVersion()
                + "\00" + pingevent.getMotd()
                + "\00" + pingevent.getCurrentPlayers()
                + "\00" + pingevent.getMaxPlayers()
                : pingevent.getMotd() + ChatColor.COLOR_CHAR + pingevent.getCurrentPlayers() + ChatColor.COLOR_CHAR + pingevent.getMaxPlayers();
        disconnect( response );
    }

    @Override
    public void handle(Packet2Handshake handshake) throws Exception
    {
        Preconditions.checkState( thisState == State.HANDSHAKE, "Not expecting HANDSHAKE" );
        this.handshake = handshake;
        stream.write( forgeMods );
        stream.write( request = EncryptionUtil.encryptRequest() );
        thisState = State.ENCRYPT;
    }

    @Override
    public void handle(PacketFCEncryptionResponse encryptResponse) throws Exception
    {
        Preconditions.checkState( thisState == State.ENCRYPT, "Not expecting ENCRYPT" );

        SecretKey shared = EncryptionUtil.getSecret( encryptResponse, request );
        if ( BungeeCord.getInstance().config.isOnlineMode() && !EncryptionUtil.isAuthenticated( handshake.username, request.serverId, shared ) )
        {
            throw new KickException( "Not authenticated with minecraft.net" );
        }

        // Check for multiple connections
        ProxiedPlayer old = ProxyServer.getInstance().getPlayer( handshake.username );
        if ( old != null )
        {
            old.disconnect( "You are already connected to the server" );
        }

        // fire login event
        LoginEvent event = new LoginEvent( this );
        ProxyServer.getInstance().getPluginManager().callEvent( event );
        if ( event.isCancelled() )
        {
            throw new KickException( event.getCancelReason() );
        }

        stream.write( new PacketFCEncryptionResponse() );
        stream = new PacketStream( new CipherInputStream( socket.getInputStream(), EncryptionUtil.getCipher( Cipher.DECRYPT_MODE, shared ) ),
                new CipherOutputStream( socket.getOutputStream(), EncryptionUtil.getCipher( Cipher.ENCRYPT_MODE, shared ) ), stream.getProtocol() );

        thisState = State.LOGIN;
    }

    @Override
    public void handle(PacketCDClientStatus clientStatus) throws Exception
    {
        Preconditions.checkState( thisState == State.LOGIN, "Not expecting LOGIN" );

        UserConnection userCon = new UserConnection( socket, this, stream, handshake, forgeLogin, loginMessages );
        ServerInfo server = ProxyServer.getInstance().getReconnectHandler().getServer( userCon );
        userCon.connect( server, true );

        thisState = State.FINISHED;
    }

    @Override
    public void run()
    {
        try
        {
            while ( thisState != State.FINISHED )
            {
                byte[] buf = stream.readPacket();
                DefinedPacket packet = DefinedPacket.packet( buf );
                packet.handle( this );
            }
        } catch ( KickException ex )
        {
            disconnect( "[Proxy - Kicked] " + ex.getMessage() );
        } catch ( EOFException ex )
        {
        } catch ( Exception ex )
        {
            disconnect( "[Proxy Error] " + Util.exception( ex ) );
            ex.printStackTrace();
        }
    }

    @Override
    public void disconnect(String reason)
    {
        thisState = State.FINISHED;
        try
        {
            stream.write( new PacketFFKick( reason ) );
        } catch ( IOException ioe )
        {
        } finally
        {
            try
            {
                socket.shutdownOutput();
                socket.close();
            } catch ( IOException ioe2 )
            {
            }
        }
    }

    @Override
    public String getName()
    {
        return ( handshake == null ) ? null : handshake.username;
    }

    @Override
    public byte getVersion()
    {
        return ( handshake == null ) ? -1 : handshake.procolVersion;
    }

    @Override
    public InetSocketAddress getVirtualHost()
    {
        return ( handshake == null ) ? null : new InetSocketAddress( handshake.host, handshake.port );
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return (InetSocketAddress) socket.getRemoteSocketAddress();
    }
}
