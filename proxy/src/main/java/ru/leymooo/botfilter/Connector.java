package ru.leymooo.botfilter;

import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.KeepAlive;
import ru.leymooo.botfilter.BotFilter.CheckState;
import ru.leymooo.botfilter.caching.PacketUtil;
import ru.leymooo.botfilter.caching.PacketUtil.KickType;
import ru.leymooo.botfilter.config.Settings;
import ru.leymooo.botfilter.packets.PlayerAbilities;
import ru.leymooo.botfilter.packets.PlayerPositionAndLook;
import ru.leymooo.botfilter.utils.ManyChecksUtils;

/**
 *
 * @author Leymooo
 */
@EqualsAndHashCode(callSuper = false, of =
{
    "name"
})
public class Connector extends MoveHandler
{

    private static long TOTAL_TICKS = 60;
    private static long TOTAL_TIME = ( TOTAL_TICKS * 50 ) - 100; //TICKS * 50MS

    private UserConnection userConnection;

    private String name;

    public int version;
    private int aticks = 0, sentPings = 0;
    public long joinTime = System.currentTimeMillis(), lastSend = 0, totalping = 0;

    public CheckState state = CheckState.CAPTCHA_ON_POSITION_FAILED;//BotFilter.getInstance().getCurrentCheckState();
    public ChannelWrapper channelWrapper;

    public Connector(UserConnection userConnection)
    {
        this.name = userConnection.getName();
        this.channelWrapper = userConnection.getCh();
        this.userConnection = userConnection;
        this.version = userConnection.getPendingConnection().getVersion();
        channelWrapper.setDecoderProtocol( Protocol.BotFilter );
        ManyChecksUtils.IncreaseOrAdd( channelWrapper.getRemoteAddress().getAddress() );
        PacketUtil.spawnPlayer( channelWrapper.getHandle(), userConnection.getPendingConnection().getVersion() );
        if ( state == CheckState.ONLY_POSITION || state == CheckState.CAPTCHA_ON_POSITION_FAILED )
        {
            channelWrapper.getHandle().write( PacketUtil.checkMessage.get( version ) );
        } else
        {
            channelWrapper.getHandle().write( PacketUtil.captchaCheckMessage.get( version ) );
        }
        sendPing();
        BotFilter.getInstance().addConnection( this );
    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        BotFilter.getInstance().removeConnection( null, this );
        disconnected( true );
    }

    @Override
    public void handlerChanged()
    {
        disconnected( true );
    }

    private void disconnected(boolean finnaly)
    {
        if ( finnaly )
        {
            userConnection = null;
        }
        channelWrapper = null;
    }

    public void completeCheck()
    {
        if ( System.currentTimeMillis() - joinTime < TOTAL_TIME )
        {
            state = CheckState.FAILED;
            PacketUtil.kickPlayer( PacketUtil.KickType.NOTPLAYER, Protocol.GAME, channelWrapper, version );
            BungeeCord.getInstance().getLogger().log( Level.INFO, "[{0}] disconnected: Too fast check passed", name );
            return;
        }

        KickType type = BotFilter.getInstance().checkIpAddress( channelWrapper.getRemoteAddress().getAddress(),
                (int) totalping / lastSend == 0 ? sentPings : sentPings - 1 );
        if ( type != null )
        {
            PacketUtil.kickPlayer( type, Protocol.GAME, channelWrapper, version );
            BungeeCord.getInstance().getLogger().log( Level.INFO, "[{0}] disconnected: {1}", new Object[]
            {
                name, ( type == KickType.PROXY ? "Proxy detected" : type == KickType.COUNTRY ? "Country is not allowed" : "Big ping" )
            } );
            return;
        }

        channelWrapper.getHandle().writeAndFlush( PacketUtil.checkSus.get( version ) );
        BotFilter.getInstance().saveUser( getName(), channelWrapper.getRemoteAddress().getAddress() );
        BotFilter.getInstance().removeConnection( null, this );
        userConnection.setNeedLogin( false );
        userConnection.getPendingConnection().finishLogin( userConnection );
        disconnected( false );
    }

    @Override
    public void onMove()
    {
        if ( lastY == -1 || state == CheckState.FAILED || state == CheckState.SUCCESSFULLY )
        {
            return;
        }
        if ( state == CheckState.ONLY_CAPTCHA && lastY != y && waitingTeleportId == -1 )
        {
            resetPosition( true );
            return;
        }
        if ( formatDouble( lastY - y ) != getSpeed( ticks ) )
        {
            if ( state == CheckState.CAPTCHA_ON_POSITION_FAILED && true == false ) //TODO: CAPTCHA
            {
                state = CheckState.ONLY_CAPTCHA;
                joinTime = System.currentTimeMillis();
                resetPosition( true );
            } else
            {
                state = CheckState.FAILED;
                PacketUtil.kickPlayer( PacketUtil.KickType.NOTPLAYER, Protocol.GAME, channelWrapper, userConnection.getPendingConnection().getVersion() );
                BungeeCord.getInstance().getLogger().log( Level.INFO, "[{0}] disconnected: Failed position check", name );
                disconnected( false );
            }
            return;
        }
        if ( y <= 25 && state == CheckState.CAPTCHA_POSITION )
        {
            resetPosition( false );
        }
        ticks++;
        if ( aticks == TOTAL_TICKS && state != CheckState.CAPTCHA_POSITION )
        {
            completeCheck();
            return;
        }
        aticks++;
    }

    private void resetPosition(boolean disableFall)
    {
        if ( disableFall )
        {
            channelWrapper.getHandle().write( PacketUtil.singlePackets.get( PlayerAbilities.class ).get( version ) );
        }
        waitingTeleportId = 9876;
        channelWrapper.getHandle().writeAndFlush( PacketUtil.singlePackets.get( PlayerPositionAndLook.class ).get( version ) );
    }

    @Override
    public void handle(KeepAlive keepAlive) throws Exception
    {
        if ( keepAlive.getRandomId() == 9876 )
        {
            if ( lastSend == 0 )
            {
                state = CheckState.FAILED;
                PacketUtil.kickPlayer( KickType.NOTPLAYER, Protocol.GAME, channelWrapper, version );
                BungeeCord.getInstance().getLogger().log( Level.INFO, "[{0}] disconnected: Tried send fake ping", name );
                return;
            }
            totalping = +( System.currentTimeMillis() - lastSend );
            lastSend = 0;
        }
    }

    public void sendPing()
    {
        if ( this.lastSend == 0 )
        {
            lastSend = System.currentTimeMillis();
            sentPings++;
            channelWrapper.getHandle().writeAndFlush( PacketUtil.singlePackets.get( KeepAlive.class ).get( version ) );
        }
    }

    public String getName()
    {
        return name.toLowerCase();
    }

    public boolean isConnected()
    {
        return channelWrapper != null && userConnection.isConnected();
    }

    @Override
    public String toString()
    {
        return "[" + name + "] <-> BotFilter";
    }
}
