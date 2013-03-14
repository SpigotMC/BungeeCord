package net.md_5.bungee.connection;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
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
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketDecoder;
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
public class InitialHandler extends PacketHandler implements PendingConnection
{

    private final ProxyServer bungee;
    private Channel ch;
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
    public void connected(Channel channel) throws Exception
    {
        this.ch = channel;
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        disconnect( ChatColor.RED + Util.exception( t ) );
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
        ServerPing response = new ServerPing( bungee.getProtocolVersion(), bungee.getGameVersion(),
                listener.getMotd(), bungee.getPlayers().size(), listener.getMaxPlayers() );

        response = bungee.getPluginManager().callEvent( new ProxyPingEvent( this, response ) ).getResponse();

        String kickMessage = ChatColor.DARK_BLUE
                + "\00" + response.getProtocolVersion()
                + "\00" + response.getGameVersion()
                + "\00" + response.getMotd()
                + "\00" + response.getCurrentPlayers()
                + "\00" + response.getMaxPlayers();
        disconnect( kickMessage );
    }

    @Override
    public void handle(Packet2Handshake handshake) throws Exception
    {
        Preconditions.checkState( thisState == State.HANDSHAKE, "Not expecting HANDSHAKE" );
        Preconditions.checkArgument( handshake.username.length() <= 16, "Cannot have username longer than 16 characters" );

        int limit = BungeeCord.getInstance().config.getPlayerLimit();
        Preconditions.checkState( limit <= 0 || bungee.getPlayers().size() < limit, "Server is full!" );

        this.handshake = handshake;
        ch.write( forgeMods );
        ch.write( request = EncryptionUtil.encryptRequest() );
        thisState = State.ENCRYPT;
    }

    @Override
    public void handle(final PacketFCEncryptionResponse encryptResponse) throws Exception
    {
        Preconditions.checkState( thisState == State.ENCRYPT, "Not expecting ENCRYPT" );

        // TODO: This is shit
        new Thread( "Login Verifier - " + getName() )
        {
            @Override
            public void run()
            {
                try
                {
                    SecretKey shared = EncryptionUtil.getSecret( encryptResponse, request );
                    if ( BungeeCord.getInstance().config.isOnlineMode() )
                    {
                        String reply = null;
                        try
                        {
                            String encName = URLEncoder.encode( InitialHandler.this.getName(), "UTF-8" );

                            MessageDigest sha = MessageDigest.getInstance( "SHA-1" );
                            for ( byte[] bit : new byte[][]
                            {
                                request.serverId.getBytes( "ISO_8859_1" ), shared.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()
                            } )
                            {
                                sha.update( bit );
                            }

                            String encodedHash = URLEncoder.encode( new BigInteger( sha.digest() ).toString( 16 ), "UTF-8" );
                            String authURL = "http://session.minecraft.net/game/checkserver.jsp?user=" + encName + "&serverId=" + encodedHash;

                            try ( BufferedReader in = new BufferedReader( new InputStreamReader( new URL( authURL ).openStream() ) ) )
                            {
                                reply = in.readLine();
                            }
                        } catch ( IOException ex )
                        {
                        }

                        if ( !"YES".equals( reply ) )
                        {
                            disconnect( "Not authenticated with Minecraft.net" );
                        }

                        // Check for multiple connections
                        ProxiedPlayer old = bungee.getPlayer( handshake.username );
                        if ( old != null )
                        {
                            old.disconnect( "You are already connected to the server" );
                        }

                        // fire login event
                        LoginEvent event = new LoginEvent( InitialHandler.this );
                        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
                        {
                            disconnect( event.getCancelReason() );
                        }
                    }

                    Cipher encrypt = EncryptionUtil.getCipher( Cipher.ENCRYPT_MODE, shared );
                    Cipher decrypt = EncryptionUtil.getCipher( Cipher.DECRYPT_MODE, shared );
                    ch.write( new PacketFCEncryptionResponse() );
                    ch.pipeline().addBefore( "decoder", "cipher", new CipherCodec( encrypt, decrypt ) );

                    thisState = InitialHandler.State.LOGIN;
                } catch ( Throwable t )
                {
                    disconnect( "[Report to md_5 / Server Owner] " + Util.exception( t ) );
                }
            }
        }.start();
    }

    @Override
    public void handle(PacketCDClientStatus clientStatus) throws Exception
    {
        Preconditions.checkState( thisState == State.LOGIN, "Not expecting LOGIN" );

        UserConnection userCon = new UserConnection( (BungeeCord) bungee, ch, this, handshake, forgeLogin, loginMessages );
        ch.pipeline().get( HandlerBoss.class ).setHandler( new UpstreamBridge( bungee, userCon ) );

        ServerInfo server = bungee.getReconnectHandler().getServer( userCon );
        userCon.connect( server, true );

        thisState = State.FINISHED;
        throw new CancelSendSignal();
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

    @Override
    public String toString()
    {
        return "[" + ( ( getName() != null ) ? getName() : getAddress() ) + "] <-> InitialHandler";
    }
}
