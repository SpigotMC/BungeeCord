package ru.leymooo.botfilter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ru.leymooo.botfilter.utils.ButtonUtils;
import ru.leymooo.botfilter.utils.Utils;
import io.netty.channel.Channel;
import java.util.ArrayList;
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
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.extra.Animation;
import net.md_5.bungee.protocol.packet.extra.ChunkPacket;
import net.md_5.bungee.protocol.packet.extra.ConfirmTransaction;
import net.md_5.bungee.protocol.packet.extra.HeldItemSlot;
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
import net.md_5.bungee.protocol.packet.extra.UpdateHeath;
import ru.leymooo.botfilter.utils.Utils.CheckState;

/**
 *
 * @author Leymooo
 */
@Data
@EqualsAndHashCode(callSuper = false, of =
{
    "name", "joinTime", "joinTimeNano"
})
public class BFConnector extends PacketHandler
{

    /* Добро пожаловать в гору говнокода и костылей */
    private UserConnection connection;
    private Channel channel;
    private long joinTime = System.currentTimeMillis(), joinTimeNano = System.nanoTime(), lastMessageSent = joinTime,
            buttonCheckStart = 0, lastPpsCheck, lastKeepAliveId = 0, lastKeepAliveSentTime = joinTime - 500, ping = -1, globalPing = 0;
    private AtomicInteger pps;
    private boolean recieved = false;
    private int globalTick = 0, localTick = 0, wrongLocations = 0, pingChecks = 0;
    private String name;
    private Location location;
    private CheckState state = CheckState.POSITION;
    private HashMap<Location, Block> buttons;
    private ArrayList<Object> checks;
    private HeldItemSlot heldItemSlot = new HeldItemSlot( 1 );
    private SetSlot setSlotPacket = new SetSlot( 0, 36, 1, 35, 0 );
    private UpdateHeath healthPacket = new UpdateHeath( 1, 1, 2 );
    private SetExp setExpPacket = new SetExp( 0.0f, 1, 1 );
    private boolean clientSettings, pluginMessage;
    //==========Статические пакеты===============
    public static DefinedPacket loginPacket = new Login( -1, (short) 2, 0, (short) 0, (short) 100, "flat", true ),
            spawnPositionPacket = new SpawnPosition( 1, 60, 1 ),
            playerPosAndLook = new PlayerPositionAndLook( 1.00, 450, 1.00, 1f, 1f, 1, false ),
            healthUpdate = new UpdateHeath( 1, 1, 0 ),
            timeUpdate,
            chat;
    public static Title checkTitle, susCheckTitle, buttonCheckTitle;
    private static List<ChunkPacket> chunkPackets = Arrays.asList(
            new ChunkPacket( 0, 0, new byte[ 256 ] ), new ChunkPacket( -1, 0, new byte[ 256 ] ), new ChunkPacket( 0, 1, new byte[ 256 ] ),
            new ChunkPacket( -1, 1, new byte[ 256 ] ), new ChunkPacket( 0, -1, new byte[ 256 ] ), new ChunkPacket( -1, -1, new byte[ 256 ] ),
            new ChunkPacket( 1, 1, new byte[ 256 ] ), new ChunkPacket( 1, -1, new byte[ 256 ] ), new ChunkPacket( 1, 0, new byte[ 256 ] ), new ChunkPacket( 2, 0, new byte[ 255 ]/*crash chunk*/ ) );
    //===========================================

    public BFConnector(UserConnection connection)
    {
        this.connection = connection;
        this.channel = this.connection.getCh().getHandle();
        this.name = this.connection.getName();
        BungeeCord.getInstance().getLogger().log( Level.INFO, "{0} has connected", toString() );
        Utils.isManyChecks( this.connection.getAddress().getAddress().getHostAddress(), true, false );
        this.connection.setClientEntityId( -1 );
        this.sendFakeServerPackets();
        Config.getConfig().getBotCounter().incrementAndGet();
        Config.getConfig().isUnderAttack();
        Config.getConfig().getConnectedUsersSet().add( this );
    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        BungeeCord.getInstance().getPluginManager().callEvent( new PlayerDisconnectEvent( connection ) );
        this.disconnected();
    }

    private void disconnected()
    {
        Config.getConfig().getConnectedUsersSet().remove( this );
        if ( state != CheckState.SUS && Utils.isManyChecks( this.connection.getAddress().getAddress().getHostAddress(), false, true ) )
        {
            Config.getConfig().getProxy().addProxyForce( getConnection().getAddress().getAddress().getHostAddress() );
        }
        this.setConnection( null );
        this.setChannel( null );
        this.setLocation( null );
        if ( this.getChecks() != null )
        {
            this.getChecks().clear();
            this.setChecks( null );
        }
    }

    private void sendFakeServerPackets()
    {
        this.write( loginPacket );
        this.write( spawnPositionPacket );
        for ( ChunkPacket chunk : chunkPackets )
        {
            this.write( chunk );
        }
        this.write( playerPosAndLook );
        this.write( timeUpdate );
        this.write( healthUpdate );
        this.write( chat );
        this.write( heldItemSlot );
        checkTitle.send( connection ); //flush используется в титлах
        trySendKeepAlive();
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

    //=======================================
    @Override
    public void handle(Animation anim)
    {
        addOrRemove( anim, true );
    }

    @Override
    public void handle(ConfirmTransaction transaction) throws Exception
    {
        if ( transaction.getWindow() == 0 && transaction.isAccepted() )
        {
            addOrRemove( transaction.getAction(), true );
        }
    }

    @Override
    public void handle(PluginMessage pluginMessage) throws Exception
    {
        if ( "MC|Brand".equals( pluginMessage.getTag() ) )
        {
            ByteBuf buf = Unpooled.wrappedBuffer( pluginMessage.getData() );
            String brand = DefinedPacket.readString( buf );
            buf.release();
            if ( brand != null && ( brand.contains( "fml" ) || brand.contains( "forge" ) || brand.equals( "vanilla" ) ) )
            {
                setPluginMessage( true );
            }
        }
    }

    @Override
    public void handle(KeepAlive packet) throws Exception
    {
        addOrRemove( packet.getRandomId(), true );
        if ( packet.getRandomId() == getLastKeepAliveId() )
        {
            this.setPing( System.currentTimeMillis() - getLastKeepAliveSentTime() );
            this.connection.setPing( (int) getPing() );
            this.setLastKeepAliveId( 0 );
            globalPing += getPing();
            ++pingChecks;
            this.sendCheckMessage( "" );
            this.trySendKeepAlive();
        }
    }

    @Override
    public void handle(ClientSettings settings) throws Exception
    {
        getConnection().setSettings( settings );
        setClientSettings( true );
    }

    @Override
    public void handle(HeldItemSlot heldSlot) throws Exception
    {
        addOrRemove( heldSlot.getSlot(), true );
    }

    //=======================================
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
        if ( getChecks() != null && getChecks().remove( (Object) conf.getTeleportId() ) )
        {
            this.recieved = true;
        }
    }

    @Override
    public void handle(PlayerPositionAndLook posRot)
    {
        if ( getLocation() == null )
        {
            setLocation( new Location( 0, 450, 0, 0, 0, false, 0 ) );
            return;
        }
        getLocation().handlePosition( posRot );
        if ( isRecieved() && getChecks().remove( posRot ) )
        {
            setRecieved( getConnection().getPendingConnection().getHandshake().getProtocolVersion() == 47 );
            localTick = 0;
            return;
        } else if ( isRecieved() && getConnection().getPendingConnection().getHandshake().getProtocolVersion() != 47 )
        {
            this.state = CheckState.FAILED;
        }
        handleY( getLocation() );
    }

    private void handleY(Location loc)
    {
        if ( state == CheckState.FAILED || state == CheckState.BUTTON || Utils.checkPps( this ) || getLocation() == null )
        {
            return;
        }
        boolean isTrue = true;
        //на всякий случай дропнем это если проверка уже была пройдена.
        if ( state != CheckState.SUS )
        {
            double formatedFallSpeed = Utils.formatDouble( loc.getLastY() - loc.getY() );
            if ( formatedFallSpeed != Utils.getFallSpeed( localTick ) )
            {
                state = CheckState.FAILED;
                return;
            }
        }
        localTick++;
        globalTick++;
        if ( ( globalTick - 1 ) >= 59 && isTrue )
        {
            if ( Config.getConfig().needButtonCheck() && ButtonUtils.getSchematic() != null )
            {
                buttonCheckTitle.send( connection );
                this.setButtonCheckStart( System.currentTimeMillis() );
                this.state = CheckState.BUTTON;
                ThreadLocalRandom random = ThreadLocalRandom.current();
                ButtonUtils.pasteSchemAndTeleportPlayer( random.nextInt( -50000, 50000 ), random.nextInt( 50, 122 ), random.nextInt( -50000, 50000 ), this );
                return;
            }
            this.state = CheckState.SUS;
            finish();
            return;
        }
        Utils.sendPackets( this );
        this.trySendKeepAlive();
        this.sendCheckPackets( false );
    }

    public void sendCheckPackets(boolean force)
    {
        if ( globalTick < 2 || globalTick > 55 || !Config.getConfig().isProtectionEnabled() || state != CheckState.POSITION )
        {
            return;
        }
        ThreadLocalRandom r = ThreadLocalRandom.current();
        if ( ( r.nextInt( 100 ) < 15 ) || force )
        {
            PlayerPositionAndLook posLook = new PlayerPositionAndLook( r.nextInt( -7, 7 ), r.nextInt( 200, 600 ), r.nextInt( -7, 7 ), 1, 1, r.nextInt( Integer.MAX_VALUE ), false );
            ConfirmTransaction confTr = new ConfirmTransaction( (byte) 0, r.nextInt( 32767 ), false );
            if ( getConnection().getPendingConnection().getHandshake().getProtocolVersion() != 47 )
            {
                this.addOrRemove( posLook.getTeleportId(), false );
            } else
            {
                setRecieved( true );
            }
            this.addOrRemove( confTr.getAction(), false );
            this.addOrRemove( posLook, false );
            this.write( posLook );
            this.channel.writeAndFlush( confTr );
        }
    }

    public void trySendKeepAlive()
    {
        if ( this.state == CheckState.SUS || this.getLastKeepAliveId() != 0 || ( ( System.currentTimeMillis() - this.lastKeepAliveSentTime ) < 250 ) )
        {
            return;
        }
        KeepAlive alive = new KeepAlive( ThreadLocalRandom.current().nextLong( Integer.MAX_VALUE ) );
        this.addOrRemove( alive.getRandomId(), false );
        this.setLastKeepAliveId( alive.getRandomId() );
        this.setLastKeepAliveSentTime( System.currentTimeMillis() );
        this.getChannel().writeAndFlush( alive, getChannel().voidPromise() );
    }

    public boolean isConnected()
    {
        return getChannel() != null && getChannel().isActive() && getConnection() != null && getConnection().isConnected();
    }

    public void addOrRemove(Object obj, boolean remove)
    {
        if ( getChecks() == null )
        {
            setChecks( new ArrayList<>() );
        }
        if ( remove )
        {
            getChecks().remove( obj );
        } else
        {
            getChecks().add( obj );
        }
    }

    public void sendCheckMessage(String message)
    {
        if ( ( System.currentTimeMillis() - lastMessageSent >= 1300 ) && !message.isEmpty() )
        {
            this.connection.sendMessage( message );
            lastMessageSent = System.currentTimeMillis();
        }
        if ( this.ping != -1 )
        {
            this.connection.sendMessage( ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText( Config.getConfig().getActionBar()
                    .replace( "%ping%", this.getPing() + "" ).replace( "%online%", Config.getConfig().getConnectedUsersSet().size() + "" ) ) );
        }

    }

    private void finish()
    {
        susCheckTitle.send( connection );
        Config config = Config.getConfig();
        //Даем на всякий случай время клиенту ответить на пакеты. 400 мс.
        if ( config.isProtectionEnabled() && state == CheckState.SUS && buttonCheckStart == 0 && !checks.isEmpty() && globalTick <= 67 )
        {
            return;
        }
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
        return "[" + getName() + "] <-> BFConnector";
    }

}
