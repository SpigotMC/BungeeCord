package ru.leymooo.gameguard;

import io.netty.channel.Channel;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.extra.ChunkPacket;
import net.md_5.bungee.protocol.packet.extra.PlayerPosition;
import net.md_5.bungee.protocol.packet.extra.PlayerPositionAndLook;
import net.md_5.bungee.protocol.packet.extra.SetExp;
import net.md_5.bungee.protocol.packet.extra.SetSlot;
import net.md_5.bungee.protocol.packet.extra.SpawnPosition;
import net.md_5.bungee.protocol.packet.extra.TeleportConfirm;
import net.md_5.bungee.protocol.packet.extra.TimeUpdate;
import net.md_5.bungee.protocol.packet.extra.UpdateHeath;

/**
 *
 * @author Leymooo
 */
@Data
@EqualsAndHashCode(callSuper = false, exclude =
{
    "connection", "wrongLocation", "channel", "lastPacketCheck", "packetsPerSec", "playerPositionAndLook", "recieved", "globalTick", "localTick", "lastY", "sus", "country", "setSlotPacket", "healthPacket", "posLook", "setExpPacket"
})
public class GGConnector extends PacketHandler
{

    /* Добро пожаловать в гору говнокода и костылей */
    private UserConnection connection;
    private Channel channel;
    private long joinTime = System.currentTimeMillis();
    private long lastPacketCheck = System.currentTimeMillis();
    private int packetsPerSec = 0;
    private boolean playerPositionAndLook = false;
    private boolean recieved = false;
    private int globalTick = 0;
    private int localTick = 0;
    private int wrongLocation = 0;
    private double lastY = 300;
    private boolean sus;
    private String country = "--";
    private String name;

    //=====================================
    private SetSlot setSlotPacket = new SetSlot( 0, 36, 57, 0 );
    private UpdateHeath healthPacket = new UpdateHeath( 1, 1, 0 );
    private SetExp setExpPacket = new SetExp( 0.0f, 1, 1 );
    private PlayerPositionAndLook posLook = null;
    private static DefinedPacket loginPacket = new Login( -1, (short) 0, 0, (short) 0, (short) 100, "flat", true ),
            spawnPositionPacket = new SpawnPosition( 1, 60, 1 ),
            playerPosAndLook = new PlayerPositionAndLook( 1.00, 300, 1.00, 1f, 1f, 1, false ),
            timeUpdate = new TimeUpdate( 1, 12000 ),
            chat = new Chat( ComponentSerializer.toString( TextComponent.fromLegacyText( Config.getConfig().getCheck() ) ), (byte) ChatMessageType.CHAT.ordinal() );
    private static List<ChunkPacket> chunkPackets = Arrays.asList(
            new ChunkPacket( 0, 0, new byte[ 256 ] ), new ChunkPacket( -1, 0, new byte[ 256 ] ), new ChunkPacket( 0, 1, new byte[ 256 ] ),
            new ChunkPacket( -1, 1, new byte[ 256 ] ), new ChunkPacket( 0, -1, new byte[ 256 ] ), new ChunkPacket( -1, -1, new byte[ 256 ] ),
            new ChunkPacket( 1, 1, new byte[ 256 ] ), new ChunkPacket( 1, -1, new byte[ 256 ] ), new ChunkPacket( 1, 0, new byte[ 256 ] ) );
    //=====================================

    public GGConnector(UserConnection connection)
    {
        this.connection = connection;
        this.channel = this.connection.getCh().getHandle();
        this.name = this.connection.getName();
        BungeeCord.getInstance().getLogger().log( Level.INFO, "{0} has connected", toString() );
        Utils.isManyChecks( this.connection.getAddress().getAddress().getHostAddress(), true );
        this.connection.setClientEntityId( -1 );
        this.sendFakeServerPackets();
        Config.getConfig().getBotCounter().incrementAndGet();
        Config.getConfig().isUnderAttack();
        Config.getConfig().getConnectedUsersSet().add( this );
        Config.getConfig().getGeoUtils().getAndSetCountryCode( this );
    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        Config.getConfig().getConnectedUsersSet().remove( this );
        if ( !isSus() )
        {
            Config.getConfig().getProxy().addProxyForce( getConnection().getAddress().getAddress().getHostAddress() );
        }
        this.setConnection( null );
        this.setChannel( null );
    }

    private void sendFakeServerPackets()
    {
        this.write( loginPacket );
        this.write( spawnPositionPacket );
        chunkPackets.forEach( chunkPacket -> this.write( chunkPacket ) );
        this.write( playerPosAndLook );
        this.write( timeUpdate );
        this.write( healthPacket );
        this.write( chat );
        this.getChannel().flush();
    }

    private void write(Object packet)
    {
        this.getChannel().write( packet, this.getChannel().voidPromise() );
    }

    @Override
    public void handle(PlayerPosition pos) throws Exception
    {
        this.handlePosition( pos.getY() );
    }

    private double getFallSpeed()
    {
        return formatDouble( Math.abs( ( Math.pow( 0.98, localTick ) - 1 ) * 3.92 ) );
    }

    private double formatDouble(double d)
    {
        return Math.floor( d * 100 ) / 100;
    }

    @Override
    public void handle(TeleportConfirm conf) throws Exception
    {
        if ( this.getPosLook() != null && this.getPosLook().getTeleportId() == conf.getTeleportId() )
        {
            this.recieved = true;
        }
    }

    @Override
    public void handle(PlayerPositionAndLook posRot)
    {
        if ( this.getPosLook() != null && isRecieved() )
        {
            PlayerPositionAndLook pos = getPosLook();
            //Костыль для 1.8
            boolean failed = false;
            if ( !( pos.getX() == posRot.getX() && pos.getY() == posRot.getY() && pos.getZ() == posRot.getZ() && pos.getYaw() == posRot.getYaw() && pos.getPitch() == posRot.getPitch() && pos.isOnGround() == posRot.isOnGround() ) )
            {
                failed = true;
                if ( getConnection().getPendingConnection().getHandshake().getProtocolVersion() != 47 )
                {
                    this.setLastY( 9999 );
                    localTick++;
                    return; //У нас бот. Портим ему на всяких случай счетчик.
                }
            }
            if ( !failed )
            {
                setPosLook( null );
                setRecieved( false );
                lastY = posRot.getY();
                localTick = 0;
                return;
            }
        }
        if ( !this.isPlayerPositionAndLook() )
        {
            this.setPlayerPositionAndLook( true );
            return;
        }
        handlePosition( posRot.getY() );
    }

    private void handlePosition(double y)
    {
        if ( isSus() )
        {
            return;
        }
        if ( checkPps() )
        {
            this.setSus( true );
            this.getConnection().disconnect( Config.getConfig().getError3() );
            return;
        }
        double formatedFallSpeed = formatDouble( lastY - y );
        if ( formatedFallSpeed != getFallSpeed() )
        {
            wrongLocation++;
        }

        if ( globalTick == 60 && formatedFallSpeed == getFallSpeed() )
        {
            this.getConnection().sendMessage( Config.getConfig().getCheckSus() );
            this.setSus( true );
            finish();
            return;
        }
        lastY = y;
        this.sendSlotPacket();
        this.sendHealthAndExpPacket();
        this.sendPosLokPacket( formatedFallSpeed == getFallSpeed() );
        localTick++;
        globalTick++;
    }

    private void sendPosLokPacket(boolean send)
    {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        if ( r.nextInt( 100 ) < 6 && globalTick < 50 && send && Config.getConfig().isUnderAttack() )
        {
            this.setPosLook( new PlayerPositionAndLook( r.nextInt( -7, 7 ), r.nextInt( 200, 600 ), r.nextInt( -7, 7 ), 1, 1, 2, false ) );
            this.getChannel().writeAndFlush( this.getPosLook() );
            if ( getConnection().getPendingConnection().getHandshake().getProtocolVersion() == 47 )
            {
                setRecieved( true );
            }
        }
    }

    private boolean checkPps()
    {
        this.setPacketsPerSec( getPacketsPerSec() + 1 );
        if ( System.currentTimeMillis() - getLastPacketCheck() <= 125 )
        {
            return getPacketsPerSec() >= 14;
        } else
        {
            setPacketsPerSec( 0 );
            setLastPacketCheck( System.currentTimeMillis() );
            return false;
        }
    }

    private void sendSlotPacket()
    {
        if ( globalTick >= 9 && globalTick <= 21 && globalTick % 2 != 0 )
        {
            this.getSetSlotPacket().setSlot( this.getSetSlotPacket().getSlot() + 1 );
            this.getChannel().writeAndFlush( this.getSetSlotPacket(), this.getChannel().voidPromise() );
        }
    }

    private void sendHealthAndExpPacket()
    {
        if ( globalTick % 3 == 0 && globalTick <= 63 )
        {
            this.getHealthPacket().setFood( this.getHealthPacket().getFood() + 1 );
            this.getHealthPacket().setHealth( this.getHealthPacket().getHealth() + 1 );
            this.write( this.getHealthPacket() );
            this.getSetExpPacket().setExpBar( this.getSetExpPacket().getExpBar() + 0.052f );
            this.getChannel().writeAndFlush( this.getSetExpPacket() );
        }
    }

    public boolean isConnected()
    {
        return getChannel() != null && getChannel().isActive() && getConnection() != null && getConnection().isConnected();
    }

    private void finish()
    {
        Config config = Config.getConfig();
        if ( ( config.isUnderAttack() || config.isPermanent() ) && ( !config.getGeoUtils().isAllowed( country, config.isPermanent() ) || config.getProxy().isProxy( getConnection().getAddress().getAddress().getHostAddress() ) ) )
        {
            getConnection().disconnect( !config.getGeoUtils().isAllowed( country, config.isPermanent() ) ? config.getError2_1() : config.getError2() );
            return;
        }
        if ( wrongLocation >= 5 )
        {
            getConnection().disconnect( config.getError1() );
            return;
        }
        config.saveIp( name, getConnection().getAddress().getAddress().getHostAddress() );
        getConnection().serverr = true;
        ( (HandlerBoss) this.getChannel().pipeline().get( HandlerBoss.class ) ).setHandler( new UpstreamBridge( ProxyServer.getInstance(), this.getConnection() ) );
        ProxyServer.getInstance().getPluginManager().callEvent( new PostLoginEvent( this.getConnection() ) );
        this.getConnection().connect( ProxyServer.getInstance().getServerInfo( this.getConnection().getPendingConnection().getListener().getDefaultServer() ), null, true );
        try
        {
            disconnected( getConnection().getCh() );
        } catch ( Exception e )
        {
            BungeeCord.getInstance().getLogger().log( Level.WARNING, "vk.com/Leymooo_s", e );
        }

    }

    @Override
    public String toString()
    {
        return "[" + getName() + "] <-> GGConnector";
    }

}
