package net.md_5.bungee;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.DataInput;
import java.security.PublicKey;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.score.Objective;
import net.md_5.bungee.api.score.Scoreboard;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.CipherDecoder;
import net.md_5.bungee.netty.CipherEncoder;
import net.md_5.bungee.netty.PacketDecoder;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.Forge;
import net.md_5.bungee.protocol.MinecraftOutput;
import net.md_5.bungee.protocol.packet.DefinedPacket;
import net.md_5.bungee.protocol.packet.Packet1Login;
import net.md_5.bungee.protocol.packet.Packet9Respawn;
import net.md_5.bungee.protocol.packet.PacketCEScoreboardObjective;
import net.md_5.bungee.protocol.packet.PacketD1Team;
import net.md_5.bungee.protocol.packet.PacketFAPluginMessage;
import net.md_5.bungee.protocol.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.protocol.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.protocol.packet.PacketFFKick;
import net.md_5.bungee.protocol.packet.forge.Forge1Login;

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
        String message = "Exception Connecting:" + Util.exception( t );
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
        if ( !user.isActive() )
        {
            ch.close();
            return;
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF( "Login" );
        out.writeUTF( user.getEffectiveAddress().getHostString() );
        out.writeInt( user.getEffectiveAddress().getPort() );
        channel.write( new PacketFAPluginMessage( "BungeeCord", out.toByteArray() ) );

        channel.write( user.getPendingConnection().getHandshake() );

        // Skip encryption if we are not using Forge
        if ( user.getPendingConnection().getForgeLogin() == null )
        {
            channel.write( PacketConstants.CLIENT_LOGIN );
        }
    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        user.getPendingConnects().remove( target );
        if( user.getServer() != null && user.getServer().isObsolete() && user.isActive() )
            BungeeCord.getInstance().executors.schedule( new Runnable() {
                @Override
                public void run() {
                    if( user.isActive() )
                        user.connect( bungee.getServerInfo( user.getPendingConnection().getListener().getFallbackServer() ), true );
                }
            }, 5, TimeUnit.SECONDS );
    }

    @Override
    public void handle(Packet1Login login) throws Exception
    {
        Preconditions.checkState( thisState == State.LOGIN, "Not expecting LOGIN" );

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

        for ( PacketFAPluginMessage message : user.getPendingConnection().getRegisterMessages() )
        {
            ch.write( message );
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
                user.setClientEntityId( login.getEntityId() );
                user.setServerEntityId( login.getEntityId() );

                // Set tab list size, this sucks balls, TODO: what shall we do about packet mutability
                Packet1Login modLogin;
                if ( ch.getHandle().pipeline().get( PacketDecoder.class ).getProtocol() == Forge.getInstance() )
                {
                    modLogin = new Forge1Login( login.getEntityId(), login.getLevelType(), login.getGameMode(), login.getDimension(), login.getDifficulty(), login.getUnused(),
                            (byte) user.getPendingConnection().getListener().getTabListSize() );
                } else
                {
                    modLogin = new Packet1Login( login.getEntityId(), login.getLevelType(), login.getGameMode(), (byte) login.getDimension(), login.getDifficulty(), login.getUnused(),
                            (byte) user.getPendingConnection().getListener().getTabListSize() );
                }
                user.unsafe().sendPacket( modLogin );

                MinecraftOutput out = new MinecraftOutput();
                out.writeString( ProxyServer.getInstance().getName() + " (" + ProxyServer.getInstance().getVersion() + ")" );
                user.unsafe().sendPacket( new PacketFAPluginMessage( "MC|Brand", out.toArray() ) );
            } else
            {
                user.getTabList().onServerChange();

                Scoreboard serverScoreboard = user.getServerSentScoreboard();
                for ( Objective objective : serverScoreboard.getObjectives() )
                {
                    user.unsafe().sendPacket( new PacketCEScoreboardObjective( objective.getName(), objective.getValue(), (byte) 1 ) );
                }
                for ( Team team : serverScoreboard.getTeams() )
                {
                    user.unsafe().sendPacket( new PacketD1Team( team.getName() ) );
                }
                serverScoreboard.clear();

                user.sendDimensionSwitch();

                user.setServerEntityId( login.getEntityId() );
                user.unsafe().sendPacket( new Packet9Respawn( login.getDimension(), login.getDifficulty(), login.getGameMode(), (short) 256, login.getLevelType() ) );

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
            byte[] token = EncryptionUtil.encrypt( publickey, encryptRequest.getVerifyToken() );

            ch.write( new PacketFCEncryptionResponse( shared, token ) );

            Cipher encrypt = EncryptionUtil.getCipher( Cipher.ENCRYPT_MODE, secretkey );
            ch.addBefore( PipelineUtils.PACKET_DECODE_HANDLER, PipelineUtils.ENCRYPT_HANDLER, new CipherEncoder( encrypt ) );

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
        ch.addBefore( PipelineUtils.PACKET_DECODE_HANDLER, PipelineUtils.DECRYPT_HANDLER, new CipherDecoder( decrypt ) );

        ch.write( user.getPendingConnection().getForgeLogin() );

        ch.write( PacketConstants.CLIENT_LOGIN );
        thisState = State.LOGIN;
    }

    @Override
    public void handle(PacketFFKick kick) throws Exception
    {
        user.getPendingConnects().remove( target );
        
        ServerInfo def = bungee.getServerInfo( user.getPendingConnection().getListener().getFallbackServer() );
        if ( Objects.equals( target, def ) )
        {
            def = null;
        }
        ServerKickEvent origEvt = new ServerKickEvent( user, kick.getMessage(), def );
        
        if( ! target.getName().equalsIgnoreCase( BungeeCord.jailServerName ) && ( kick.getMessage().contains( "Server" ) || kick.getMessage().contains( "closed" ) || kick.getMessage().contains( "white-listed" ) ) && user.getServer() == null )
            origEvt.setCancelled( true );
        
        ServerKickEvent event = bungee.getPluginManager().callEvent( origEvt );
        if ( event.isCancelled() && event.getCancelServer() != null )
        {
            user.connect( event.getCancelServer() );
            return;
        }

        String message = bungee.getTranslation( "connect_kick" ) + target.getName() + ": " + kick.getMessage();
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
        if ( pluginMessage.equals( PacketConstants.I_AM_BUNGEE ) )
        {
            throw new IllegalStateException( "May not connect to another BungeCord!" );
        }

        DataInput in = pluginMessage.getStream();
        if ( pluginMessage.getTag().equals( "FML" ) && in.readUnsignedByte() == 0 )
        {
            int count = in.readInt();
            for ( int i = 0; i < count; i++ )
            {
                in.readUTF();
            }
            if ( in.readByte() != 0 )
            {
                // TODO: Using forge flag
                ch.getHandle().pipeline().get( PacketDecoder.class ).setProtocol( Forge.getInstance() );
            }
        }

        user.unsafe().sendPacket( pluginMessage ); // We have to forward these to the user, especially with Forge as stuff might break
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
