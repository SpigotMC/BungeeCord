package net.md_5.bungee;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import java.io.EOFException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.netty.CipherCodec;
import net.md_5.bungee.netty.PacketDecoder;
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
import net.md_5.bungee.protocol.PacketDefinitions;

@RequiredArgsConstructor
public class InitialHandler extends PacketHandler implements Runnable, PendingConnection
{

    private final ProxyServer bungee;
    private final Channel ch;
    @Getter
    private final ListenerInfo listener;
    private Packet1Login forgeLogin;
    private Packet2Handshake handshake;
    private PacketFDEncryptionRequest request;
    private List<PacketFAPluginMessage> loginMessages = new ArrayList<>();
    private State thisState = State.HANDSHAKE;
    private static final PacketFAPluginMessage forgeMods = new PacketFAPluginMessage( "FML", new byte[]
    {
        0, 0, 0, 0, 0, 2
    } );

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

        ch.pipeline().get( PacketDecoder.class ).setProtocol( PacketDefinitions.FORGE_PROTOCOL );
    }

    @Override
    public void handle(PacketFAPluginMessage pluginMessage) throws Exception
    {
        loginMessages.add( pluginMessage );
    }

    @Override
    public void handle(PacketFEPing ping) throws Exception
    {
        ServerPing pingevent = new ServerPing( BungeeCord.PROTOCOL_VERSION, BungeeCord.GAME_VERSION,
                listener.getMotd(), bungee.getPlayers().size(), listener.getMaxPlayers() );

        pingevent = bungee.getPluginManager().callEvent( new ProxyPingEvent( this, pingevent ) ).getResponse();

        String response = ChatColor.COLOR_CHAR + "1"
                + "\00" + pingevent.getProtocolVersion()
                + "\00" + pingevent.getGameVersion()
                + "\00" + pingevent.getMotd()
                + "\00" + pingevent.getCurrentPlayers()
                + "\00" + pingevent.getMaxPlayers();
        disconnect( response );
    }

    @Override
    public void handle(Packet2Handshake handshake) throws Exception
    {
        Preconditions.checkState( thisState == State.HANDSHAKE, "Not expecting HANDSHAKE" );
        Preconditions.checkArgument( handshake.username.length() <= 16, "Cannot have username longer than 16 characters" );
        this.handshake = handshake;
        ch.write( forgeMods );
        ch.write( request = EncryptionUtil.encryptRequest() );
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
        ProxiedPlayer old = bungee.getInstance().getPlayer( handshake.username );
        if ( old != null )
        {
            old.disconnect( "You are already connected to the server" );
        }

        // fire login event
        LoginEvent event = new LoginEvent( this );
        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            disconnect( event.getCancelReason() );
        }

        ch.write( new PacketFCEncryptionResponse() );

        Cipher decrypt = EncryptionUtil.getCipher( Cipher.DECRYPT_MODE, shared );
        Cipher encrypt = EncryptionUtil.getCipher( Cipher.ENCRYPT_MODE, shared );
        ch.pipeline().addBefore( "decoder", "cipher", new CipherCodec( encrypt, decrypt ) );

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
    public synchronized void disconnect(String reason)
    {
        if ( ch.isActive() )
        {
            ch.write( new PacketFFKick( reason ) );
            ch.close();
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
        return (InetSocketAddress) ch.remoteAddress();
    }
}
