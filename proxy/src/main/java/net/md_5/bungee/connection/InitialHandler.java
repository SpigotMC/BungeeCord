package net.md_5.bungee.connection;

import com.google.common.base.Preconditions;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.PacketConstants;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.CipherDecoder;
import net.md_5.bungee.netty.CipherEncoder;
import net.md_5.bungee.netty.PacketDecoder;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.Forge;
import net.md_5.bungee.protocol.packet.DefinedPacket;
import net.md_5.bungee.protocol.packet.Packet1Login;
import net.md_5.bungee.protocol.packet.Packet2Handshake;
import net.md_5.bungee.protocol.packet.PacketCDClientStatus;
import net.md_5.bungee.protocol.packet.PacketFAPluginMessage;
import net.md_5.bungee.protocol.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.protocol.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.protocol.packet.PacketFEPing;
import net.md_5.bungee.protocol.packet.PacketFFKick;

@RequiredArgsConstructor
public class InitialHandler extends PacketHandler implements PendingConnection
{

    private final ProxyServer bungee;
    private ChannelWrapper ch;
    @Getter
    private final ListenerInfo listener;
    @Getter
    private Packet1Login forgeLogin;
    @Getter
    private Packet2Handshake handshake;
    private PacketFDEncryptionRequest request;
    @Getter
    private List<PacketFAPluginMessage> loginMessages = new ArrayList<>();
    private State thisState = State.HANDSHAKE;
    private SecretKey sharedKey;
    private final Unsafe unsafe = new Unsafe()
    {
        @Override
        public void sendPacket(DefinedPacket packet)
        {
            ch.write( packet );
        }
    };

    private enum State
    {

        HANDSHAKE, ENCRYPT, LOGIN, FINISHED;
    }

    @Override
    public void connected(ChannelWrapper channel) throws Exception
    {
        this.ch = channel;
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        disconnect( ChatColor.RED + Util.exception( t ) );
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
                listener.getMotd(), bungee.getOnlineCount(), listener.getMaxPlayers() );

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
    public void handle(Packet1Login login) throws Exception
    {
        Preconditions.checkState( thisState == State.LOGIN, "Not expecting FORGE LOGIN" );
        Preconditions.checkState( forgeLogin == null, "Already received FORGE LOGIN" );
        forgeLogin = login;

        ch.getHandle().pipeline().get( PacketDecoder.class ).setProtocol( Forge.getInstance() );
    }

    @Override
    public void handle(Packet2Handshake handshake) throws Exception
    {
        Preconditions.checkState( thisState == State.HANDSHAKE, "Not expecting HANDSHAKE" );
        this.handshake = handshake;
        bungee.getLogger().log( Level.INFO, "{0} has connected", this );

        if ( handshake.getUsername().length() > 16 )
        {
            disconnect( "Cannot have username longer than 16 characters" );
            return;
        }

        int limit = BungeeCord.getInstance().config.getPlayerLimit();
        if ( limit > 0 && bungee.getOnlineCount() > limit )
        {
            disconnect( bungee.getTranslation( "proxy_full" ) );
            return;
        }

        // If offline mode and they are already on, don't allow connect
        if ( !BungeeCord.getInstance().config.isOnlineMode() && bungee.getPlayer( handshake.getUsername() ) != null )
        {
            disconnect( bungee.getTranslation( "already_connected" ) );
            return;
        }

        unsafe().sendPacket( PacketConstants.I_AM_BUNGEE );
        unsafe().sendPacket( PacketConstants.FORGE_MOD_REQUEST );
        unsafe().sendPacket( request = EncryptionUtil.encryptRequest() );
        thisState = State.ENCRYPT;
    }

    @Override
    public void handle(final PacketFCEncryptionResponse encryptResponse) throws Exception
    {
        Preconditions.checkState( thisState == State.ENCRYPT, "Not expecting ENCRYPT" );

        sharedKey = EncryptionUtil.getSecret( encryptResponse, request );
        Cipher decrypt = EncryptionUtil.getCipher( Cipher.DECRYPT_MODE, sharedKey );
        ch.getHandle().pipeline().addBefore( "decoder", "decrypt", new CipherDecoder( decrypt ) );

        if ( BungeeCord.getInstance().config.isOnlineMode() )
        {
            String encName = URLEncoder.encode( InitialHandler.this.getName(), "UTF-8" );

            MessageDigest sha = MessageDigest.getInstance( "SHA-1" );
            for ( byte[] bit : new byte[][]
            {
                request.getServerId().getBytes( "ISO_8859_1" ), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()
            } )
            {
                sha.update( bit );
            }

            String encodedHash = URLEncoder.encode( new BigInteger( sha.digest() ).toString( 16 ), "UTF-8" );
            String authURL = "http://session.minecraft.net/game/checkserver.jsp?user=" + encName + "&serverId=" + encodedHash;
            bungee.getHttpClient().prepareGet( authURL ).execute( new AsyncCompletionHandler<Response>()
            {
                @Override
                public Response onCompleted(Response response) throws Exception
                {
                    if ( "YES".equals( response.getResponseBody() ) )
                    {
                        finish();
                    } else
                    {
                        disconnect( "Not authenticated with Minecraft.net" );
                    }
                    return response;
                }

                @Override
                public void onThrowable(Throwable t)
                {
                    disconnect( "Error occured while contacting login servers, are they down?" );
                    bungee.getLogger().log( Level.SEVERE, "Error authenticating " + getName() + " with minecraft.net", t );
                }
            } );
        } else
        {
            finish();
        }
    }

    private void finish() throws GeneralSecurityException
    {
        // Check for multiple connections
        ProxiedPlayer old = bungee.getPlayer( handshake.getUsername() );
        if ( old != null )
        {
            old.disconnect( bungee.getTranslation( "already_connected" ) );
        }

        Callback<LoginEvent> complete = new Callback<LoginEvent>()
        {
            @Override
            public void done(LoginEvent result, Throwable error)
            {
                if ( result.isCancelled() )
                {
                    disconnect( result.getCancelReason() );
                }
                if ( ch.isClosed() )
                {
                    return;
                }

                thisState = InitialHandler.State.LOGIN;
                unsafe().sendPacket( new PacketFCEncryptionResponse( new byte[ 0 ], new byte[ 0 ] ) );
                try
                {
                    Cipher encrypt = EncryptionUtil.getCipher( Cipher.ENCRYPT_MODE, sharedKey );
                    ch.getHandle().pipeline().addBefore( "decoder", "encrypt", new CipherEncoder( encrypt ) );
                } catch ( GeneralSecurityException ex )
                {
                    disconnect( "Cipher error: " + Util.exception( ex ) );
                }
            }
        };

        // fire login event
        bungee.getPluginManager().callEvent( new LoginEvent( InitialHandler.this, complete ) );
    }

    @Override
    public void handle(PacketCDClientStatus clientStatus) throws Exception
    {
        Preconditions.checkState( thisState == State.LOGIN, "Not expecting LOGIN" );

        UserConnection userCon = new UserConnection( (BungeeCord) bungee, ch, getName(), this );
        userCon.init();

        bungee.getPluginManager().callEvent( new PostLoginEvent( userCon ) );

        ch.getHandle().pipeline().get( HandlerBoss.class ).setHandler( new UpstreamBridge( bungee, userCon ) );

        ServerInfo server = bungee.getReconnectHandler().getServer( userCon );
        userCon.connect( server, true );

        thisState = State.FINISHED;
        throw new CancelSendSignal();
    }

    @Override
    public synchronized void disconnect(String reason)
    {
        if ( !ch.isClosed() )
        {
            unsafe().sendPacket( new PacketFFKick( reason ) );
            ch.close();
        }
    }

    @Override
    public String getName()
    {
        return ( handshake == null ) ? null : handshake.getUsername();
    }

    @Override
    public byte getVersion()
    {
        return ( handshake == null ) ? -1 : handshake.getProcolVersion();
    }

    @Override
    public InetSocketAddress getVirtualHost()
    {
        return ( handshake == null ) ? null : new InetSocketAddress( handshake.getHost(), handshake.getPort() );
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return (InetSocketAddress) ch.getHandle().remoteAddress();
    }

    @Override
    public Unsafe unsafe()
    {
        return unsafe;
    }

    @Override
    public String toString()
    {
        return "[" + ( ( getName() != null ) ? getName() : getAddress() ) + "] <-> InitialHandler";
    }
}
