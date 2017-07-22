package ru.leymooo.gameguard;

import ru.leymooo.gameguard.utils.ButtonUtils;
import ru.leymooo.gameguard.utils.Utils;
import io.netty.channel.Channel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
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
import net.md_5.bungee.protocol.packet.extra.MultiBlockChange.Block;
import net.md_5.bungee.protocol.packet.extra.Player;
import net.md_5.bungee.protocol.packet.extra.PlayerLook;
import net.md_5.bungee.protocol.packet.extra.PlayerPosition;
import net.md_5.bungee.protocol.packet.extra.PlayerPositionAndLook;
import net.md_5.bungee.protocol.packet.extra.PlayerTryUseItemOnBlock;
import net.md_5.bungee.protocol.packet.extra.SetExp;
import net.md_5.bungee.protocol.packet.extra.SetSlot;
import net.md_5.bungee.protocol.packet.extra.SpawnPosition;
import net.md_5.bungee.protocol.packet.extra.TeleportConfirm;
import net.md_5.bungee.protocol.packet.extra.TimeUpdate;
import net.md_5.bungee.protocol.packet.extra.UpdateHeath;
import ru.leymooo.gameguard.utils.Utils.CheckState;

/**
 *
 * @author Leymooo
 */
@Data
@EqualsAndHashCode(callSuper = false, exclude =
{
    "buttons", "location", "state", "connection", "wrongLocations", "channel", "lastPacketCheck", "packets", "playerPositionAndLook", "recieved", "globalTick", "localTick", "setSlotPacket", "healthPacket", "posLook", "setExpPacket"
})
public class GGConnector extends PacketHandler
{

    /* Добро пожаловать в гору говнокода и костылей */
    private UserConnection connection;
    private Channel channel;
    private long joinTime = System.currentTimeMillis();
    private long lastPacketCheck = System.currentTimeMillis();
    private AtomicInteger packets;
    private boolean playerPositionAndLook = false;
    private boolean recieved = false;
    private int globalTick = 0;
    private int localTick = 0;
    private int wrongLocations = 0;
    private String name;
    private Location location;
    private CheckState state = CheckState.POSITION;
    private HashMap<Location, Block> buttons;
    //=====================================
    private SetSlot setSlotPacket;
    private UpdateHeath healthPacket;
    private SetExp setExpPacket;
    private PlayerPositionAndLook posLook = null;
    private static DefinedPacket loginPacket = new Login( -1, (short) 2, 0, (short) 0, (short) 100, "flat", true ),
            spawnPositionPacket = new SpawnPosition( 1, 60, 1 ),
            playerPosAndLook = new PlayerPositionAndLook( 1.00, 300, 1.00, 1f, 1f, 1, false ),
            timeUpdate = new TimeUpdate( 1, 1000 ),
            healthUpdate = new UpdateHeath( 1, 1, 0 ),
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
    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        this.disconnected();
    }

    private void disconnected()
    {
        Config.getConfig().getConnectedUsersSet().remove( this );
        if ( state != CheckState.SUS )
        {
            Config.getConfig().getProxy().addProxyForce( getConnection().getAddress().getAddress().getHostAddress() );
        }
        this.setConnection( null );
        this.setChannel( null );
        this.setLocation( null );
    }

    private void sendFakeServerPackets()
    {
        this.write( loginPacket );
        this.write( spawnPositionPacket );
        chunkPackets.forEach( chunkPacket -> this.write( chunkPacket ) );
        this.write( playerPosAndLook );
        this.write( timeUpdate );
        this.write( healthUpdate );
        this.write( chat );
        this.getChannel().flush();
    }

    public void write(Object packet)
    {
        this.getChannel().write( packet, this.getChannel().voidPromise() );
    }

    @Override
    public void handle(PlayerTryUseItemOnBlock blockClick) throws Exception
    {
        if ( state == CheckState.BUTTON )
        {
            Location loc = Location.LocationFromLong( blockClick.getLocation() );
            Block block = buttons.get( loc );
            if ( block == null )
            {
                return;
            }
            if ( block.getBlockData() == 5 )
            {
                if ( Utils.canUseButton( getLocation(), loc ) )
                {
                    state = CheckState.SUS;
                    finish();
                    return;
                }
                getConnection().disconnect( Config.getConfig().getErrorCantUse() );
                state = CheckState.FAILED;
                return;
            }
            getConnection().disconnect( Config.getConfig().getErrorWrongButton() );
            state = CheckState.FAILED;
        }
    }

    @Override
    public void handle(Player player) throws Exception
    {
        if ( getLocation() != null && state != CheckState.FAILED )
        {
            getLocation().handlePosition( player );
        }
    }

    @Override
    public void handle(PlayerLook look) throws Exception
    {
        if ( getLocation() != null && state != CheckState.FAILED )
        {
            getLocation().handlePosition( look );
        }
    }

    @Override
    public void handle(PlayerPosition pos) throws Exception
    {
        if ( getLocation() != null && state != CheckState.FAILED )
        {
            getLocation().handlePosition( pos );
            this.handleY( getLocation() );
        }
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
        if ( getLocation() == null )
        {
            setLocation( new Location( 0, 300, 0, 0, 0, false, 0 ) );
            return;
        }
        getLocation().handlePosition( posRot );
        if ( this.getPosLook() != null && isRecieved() )
        {
            PlayerPositionAndLook pos = getPosLook();
            if ( pos.equals( posRot ) )
            {
                setPosLook( null );
                setRecieved( false );
                localTick = 0;
                return;
            } else if ( getConnection().getPendingConnection().getHandshake().getProtocolVersion() != 47 )
            {
                this.state = CheckState.FAILED;
            }
        }
        handleY( getLocation() );
    }

    private void handleY(Location loc)
    {
        if ( state != CheckState.POSITION || Utils.checkPps( this ) || getLocation() == null )
        {
            return;
        }
        double formatedFallSpeed = Utils.formatDouble( loc.getLastY() - loc.getY() );
        boolean isTrue = formatedFallSpeed == Utils.getFallSpeed( localTick );
        if ( !isTrue && wrongLocations++ >= 5 )
        {
            state = CheckState.FAILED;
            return;
        }
        if ( globalTick == 60 && isTrue )
        {
            if ( Config.getConfig().isUnderAttack() && ButtonUtils.getSchematic() != null)
            {
                this.state = CheckState.BUTTON;
                ThreadLocalRandom random = ThreadLocalRandom.current();
                ButtonUtils.pasteSchemAndTeleportPlayer( random.nextInt( -3000, 3000 ), random.nextInt( 50, 122 ), random.nextInt( -3000, 3000 ), this );
                return;
            }
            this.state = CheckState.SUS;
            finish();
            return;
        }
        Utils.sendPackets( this );
        this.sendPosLokPacket( isTrue );
        localTick++;
        globalTick++;
    }

    private void sendPosLokPacket(boolean send)
    {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        if ( r.nextInt( 100 ) < 6 && globalTick < 50 && send && Config.getConfig().isUnderAttack() && getPosLook() == null )
        {
            this.setPosLook( new PlayerPositionAndLook( r.nextInt( -7, 7 ), r.nextInt( 200, 600 ), r.nextInt( -7, 7 ), 1, 1, 2, false ) );
            setRecieved( getConnection().getPendingConnection().getHandshake().getProtocolVersion() == 47 );
            this.getChannel().writeAndFlush( this.getPosLook() );
        }
    }

    public boolean isConnected()
    {
        return getChannel() != null && getChannel().isActive() && getConnection() != null && getConnection().isConnected();
    }

    private void finish()
    {
        Config config = Config.getConfig();
        getConnection().sendMessage( config.getCheckSus() );
        if ( Utils.disconnect( this ) )
        {
            return;
        }
        config.saveIp( name, getConnection().getAddress().getAddress().getHostAddress() );
        getConnection().serverr = true;
        ( (HandlerBoss) this.getChannel().pipeline().get( HandlerBoss.class ) ).setHandler( new UpstreamBridge( ProxyServer.getInstance(), this.getConnection() ) );
        ProxyServer.getInstance().getPluginManager().callEvent( new PostLoginEvent( this.getConnection() ) );
        this.getConnection().connect( ProxyServer.getInstance().getServerInfo( this.getConnection().getPendingConnection().getListener().getDefaultServer() ), null, true );
        this.disconnected();

    }

    @Override
    public String toString()
    {
        return "[" + getName() + "] <-> GGConnector";
    }

}
