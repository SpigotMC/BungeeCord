package ru.leymooo.botfilter;

import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.Protocol;
import ru.leymooo.botfilter.BotFilter.CheckState;
import ru.leymooo.botfilter.caching.PacketUtil;
import ru.leymooo.botfilter.caching.PacketUtil.KickType;
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

    private int aticks = 0;

    private boolean isFinished = false;

    public CheckState state = CheckState.CAPTCHA_ON_POSITION_FAILED;//BotFilter.getInstance().getCurrentCheckState();
    public long joinTime = System.currentTimeMillis();
    public ChannelWrapper channelWrapper;
    public int version;

    public Connector(UserConnection userConnection)
    {
        this.name = userConnection.getName();
        this.channelWrapper = userConnection.getCh();
        this.userConnection = userConnection;
        this.version = userConnection.getPendingConnection().getVersion();
        channelWrapper.setDecoderProtocol( Protocol.BotFilter );
        BotFilter.getInstance().addConnection( this );
        ManyChecksUtils.IncreaseOrAdd( channelWrapper.getRemoteAddress().getAddress() );
        PacketUtil.spawnPlayer( channelWrapper.getHandle(), userConnection.getPendingConnection().getVersion() );
        if ( state == CheckState.ONLY_POSITION || state == CheckState.CAPTCHA_ON_POSITION_FAILED )
        {
            channelWrapper.getHandle().writeAndFlush( PacketUtil.checkMessage.get( version ) );
        } else
        {
            channelWrapper.getHandle().writeAndFlush( PacketUtil.captchaCheckMessage.get( version ) );
        }
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
        System.out.println( "aaa?" );
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

        KickType type = BotFilter.getInstance().checkIpAddress( channelWrapper.getRemoteAddress().getAddress() );
        if ( type != null )
        {
            PacketUtil.kickPlayer( type, Protocol.GAME, channelWrapper, version );
            BungeeCord.getInstance().getLogger().log( Level.INFO, "[{0}] disconnected: {1}", new Object[]
            {
                name, ( type == KickType.PROXY ? "Proxy detected" : "Country is not allowed" )
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
        if ( lastY == -1 || lastY == y || isFinished || state == CheckState.ONLY_CAPTCHA || state == CheckState.FAILED || state == CheckState.SUCCESSFULLY )
        {
            return;
        }
        if ( formatDouble( lastY - y ) != getSpeed( ticks ) )
        {
            System.out.println( formatDouble( lastY - y ) + ";" + getSpeed( ticks ) );
            if ( state == CheckState.CAPTCHA_ON_POSITION_FAILED && true == false) //TODO: CAPTCHA
            {
                state = CheckState.ONLY_CAPTCHA;
                joinTime = System.currentTimeMillis();
            } else
            {
                state = CheckState.FAILED;
                PacketUtil.kickPlayer( PacketUtil.KickType.NOTPLAYER, Protocol.GAME, channelWrapper, userConnection.getPendingConnection().getVersion() );
                BungeeCord.getInstance().getLogger().log( Level.INFO, "[{0}] disconnected: Failed position check", name );
                disconnected( false );
            }
            return;
        }
        ticks++;
        if ( aticks == TOTAL_TICKS && state != CheckState.CAPTCHA_POSITION )
        {
            completeCheck();
            return;
        }
        aticks++;
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
