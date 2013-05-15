package net.md_5.bungee;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.security.PublicKey;
import java.util.Objects;
import java.util.Queue;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.scoreboard.Objective;
import net.md_5.bungee.api.scoreboard.Scoreboard;
import net.md_5.bungee.api.scoreboard.Team;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.CipherDecoder;
import net.md_5.bungee.netty.CipherEncoder;
import net.md_5.bungee.netty.PacketDecoder;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.Packet9Respawn;
import net.md_5.bungee.packet.PacketCDClientStatus;
import net.md_5.bungee.packet.PacketCEScoreboardObjective;
import net.md_5.bungee.packet.PacketD1Team;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketHandler;
import net.md_5.bungee.protocol.PacketDefinitions;

@RequiredArgsConstructor
public class ServerConnector extends PacketHandler
{

    private final ProxyServer bungee;
    private ChannelWrapper ch;
    private final UserConnection user;
    private final BungeeServerInfo target;
    private State thisState = State.ENCRYPT_REQUEST;
    private SecretKey secretkey;
    private boolean sentMessages;

    private enum State
    {

        ENCRYPT_REQUEST, ENCRYPT_RESPONSE, LOGIN, FINISHED;
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        String message = "Exception Connectiong:" + Util.exception( t );
        if ( user.getServer() == null )
        {
            user.disconnect( message );
        } else
        {
            user.sendMessage( ChatColor.RED + message );
        }
    }

    @Override
    public void connected(ChannelWrapper channel) throws Exception
    {
        this.ch = channel;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF( "Login" );
        out.writeUTF( user.getAddress().getAddress().getHostAddress() );
        out.writeInt( user.getAddress().getPort() );
        channel.write( new PacketFAPluginMessage( "BungeeCord", out.toByteArray() ) );

        channel.write( user.getPendingConnection().getHandshake() );

        // Skip encryption if we are not using Forge
        if ( user.getPendingConnection().getForgeLogin() == null )
        {
            channel.write( PacketCDClientStatus.CLIENT_LOGIN );
        }
    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        user.getPendingConnects().remove( target );
    }

    @Override
    public void handle(Packet1Login login) throws Exception
    {
        Preconditions.checkState( thisState == State.LOGIN, "Not exepcting LOGIN" );

        ServerConnection server = new ServerConnection( ch, target );
        ServerConnectedEvent event = new ServerConnectedEvent( user, server );
        bungee.getPluginManager().callEvent( event );

        ch.write( BungeeCord.getInstance().registerChannels() );
        Queue<DefinedPacket> packetQueue = target.getPacketQueue();
        synchronized ( packetQueue )
        {
            while ( !packetQueue.isEmpty() )
            {
                ch.write( packetQueue.poll() );
            }
        }

        if ( !sentMessages )
        {
            for ( PacketFAPluginMessage message : user.getPendingConnection().getLoginMessages() )
            {
                ch.write( message );
            }
        }

        if ( user.getSettings() != null )
        {
            ch.write( user.getSettings() );
        }

        synchronized ( user.getSwitchMutex() )
        {
            if ( user.getServer() == null )
            {
                // Once again, first connection
                user.setClientEntityId( login.entityId );
                user.setServerEntityId( login.entityId );
                // Set tab list size
                Packet1Login modLogin = new Packet1Login(
                        login.entityId,
                        login.levelType,
                        login.gameMode,
                        (byte) login.dimension,
                        login.difficulty,
                        login.unused,
                        (byte) user.getPendingConnection().getListener().getTabListSize(),
                        ch.getHandle().pipeline().get( PacketDecoder.class ).getProtocol() == PacketDefinitions.FORGE_PROTOCOL );
                user.sendPacket( modLogin );
            } else
            {
                bungee.getTabListHandler().onServerChange( user );

                Scoreboard serverScoreboard = user.getServerSentScoreboard();
                for ( Objective objective : serverScoreboard.getObjectives() )
                {
                    user.sendPacket( new PacketCEScoreboardObjective( objective.getName(), objective.getValue(), (byte) 1 ) );
                }
                for ( Team team : serverScoreboard.getTeams() )
                {
                    user.sendPacket( PacketD1Team.destroy( team.getName() ) );
                }
                serverScoreboard.clear();

                user.sendPacket( Packet9Respawn.DIM1_SWITCH );
                user.sendPacket( Packet9Respawn.DIM2_SWITCH );

                user.setServerEntityId( login.entityId );
                user.sendPacket( new Packet9Respawn( login.dimension, login.difficulty, login.gameMode, (short) 256, login.levelType ) );

                // Remove from old servers
                user.getServer().setObsolete( true );
                user.getServer().disconnect( "Quitting" );
            }

            // TODO: Fix this?
            if ( !user.isActive() )
            {
                server.disconnect( "Quitting" );
                // Silly server admins see stack trace and die
                bungee.getLogger().warning( "No client connected for pending server!" );
                return;
            }

            // Add to new server
            // TODO: Move this to the connected() method of DownstreamBridge
            target.addPlayer( user );
            user.getPendingConnects().remove( target );

            user.setServer( server );
            ch.getHandle().pipeline().get( HandlerBoss.class ).setHandler( new DownstreamBridge( bungee, user, server ) );
        }

        bungee.getPluginManager().callEvent( new ServerSwitchEvent( user ) );

        thisState = State.FINISHED;

        throw new CancelSendSignal();
    }

    @Override
    public void handle(PacketFDEncryptionRequest encryptRequest) throws Exception
    {
        Preconditions.checkState( thisState == State.ENCRYPT_REQUEST, "Not expecting ENCRYPT_REQUEST" );

        // Only need to handle this if we want to use encryption
        if ( user.getPendingConnection().getForgeLogin() != null )
        {
            PublicKey publickey = EncryptionUtil.getPubkey( encryptRequest );
            this.secretkey = EncryptionUtil.getSecret();

            byte[] shared = EncryptionUtil.encrypt( publickey, secretkey.getEncoded() );
            byte[] token = EncryptionUtil.encrypt( publickey, encryptRequest.verifyToken );

            ch.write( new PacketFCEncryptionResponse( shared, token ) );

            Cipher encrypt = EncryptionUtil.getCipher( Cipher.ENCRYPT_MODE, secretkey );
            ch.getHandle().pipeline().addBefore( "decoder", "encrypt", new CipherEncoder( encrypt ) );

            thisState = State.ENCRYPT_RESPONSE;
        } else
        {
            thisState = State.LOGIN;
        }
    }

    @Override
    public void handle(PacketFCEncryptionResponse encryptResponse) throws Exception
    {
        Preconditions.checkState( thisState == State.ENCRYPT_RESPONSE, "Not expecting ENCRYPT_RESPONSE" );

        Cipher decrypt = EncryptionUtil.getCipher( Cipher.DECRYPT_MODE, secretkey );
        ch.getHandle().pipeline().addBefore( "decoder", "decrypt", new CipherDecoder( decrypt ) );

        ch.write( user.getPendingConnection().getForgeLogin() );

        ch.write( PacketCDClientStatus.CLIENT_LOGIN );
        thisState = State.LOGIN;
    }

    @Override
    public void handle(PacketFFKick kick) throws Exception
    {
        ServerInfo def = bungee.getServerInfo( user.getPendingConnection().getListener().getFallbackServer() );
        if ( Objects.equals( target, def ) )
        {
            def = null;
        }
        ServerKickEvent event = bungee.getPluginManager().callEvent( new ServerKickEvent( user, kick.message, def ) );
        if ( event.isCancelled() && event.getCancelServer() != null )
        {
            user.connect( event.getCancelServer() );
            return;
        }

        String message = bungee.getTranslation( "connect_kick" ) + target.getName() + ": " + kick.message;
        if ( user.getServer() == null )
        {
            user.disconnect( message );
        } else
        {
            user.sendMessage( message );
        }
    }

    @Override
    public void handle(PacketFAPluginMessage pluginMessage) throws Exception
    {
        if ( ( pluginMessage.data[0] & 0xFF ) == 0 && pluginMessage.tag.equals( "FML" ) )
        {
            ByteArrayDataInput in = ByteStreams.newDataInput( pluginMessage.data );
            in.readUnsignedByte();
            int count = in.readInt();
            for ( int i = 0; i < count; i++ )
            {
                in.readUTF();
            }
            if ( in.readByte() != 0 )
            {
                // TODO: Using forge flag
                ch.getHandle().pipeline().get( PacketDecoder.class ).setProtocol( PacketDefinitions.FORGE_PROTOCOL );
            }
        }

        user.sendPacket( pluginMessage ); // We have to forward these to the user, especially with Forge as stuff might break
        if ( !sentMessages && user.getPendingConnection().getForgeLogin() != null )
        {
            for ( PacketFAPluginMessage message : user.getPendingConnection().getLoginMessages() )
            {
                ch.write( message );
            }
            sentMessages = true;
        }
    }

    @Override
    public String toString()
    {
        return "[" + user.getName() + "] <-> ServerConnector [" + target.getName() + "]";
    }
}
