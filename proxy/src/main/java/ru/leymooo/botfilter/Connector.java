package ru.leymooo.botfilter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.KeepAlive;
import ru.leymooo.botfilter.BotFilter.CheckState;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.caching.PacketUtils.KickType;
import ru.leymooo.botfilter.packets.PlayerAbilities;
import ru.leymooo.botfilter.packets.PlayerPositionAndLook;
import ru.leymooo.botfilter.packets.SetSlot;
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

    private static long TOTAL_TICKS = 100;
    private static long TOTAL_TIME = ( TOTAL_TICKS * 50 ) - 100; //TICKS * 50MS

    public UserConnection userConnection;

    public String name;

    public int version;
    private int aticks = 0, sentPings = 0, captchaAnswer, attemps = 3;
    public long joinTime = System.currentTimeMillis(), lastSend = 0, totalping = 9999;

    public CheckState state = BotFilter.getInstance().getCurrentCheckState();
    public ChannelWrapper channelWrapper;

    private ThreadLocalRandom random = ThreadLocalRandom.current();

    public Connector(UserConnection userConnection)
    {
        this.name = userConnection.getName();
        this.channelWrapper = userConnection.getCh();
        this.userConnection = userConnection;
        this.version = userConnection.getPendingConnection().getVersion();
        channelWrapper.setDecoderProtocol( Protocol.BotFilter );
        ManyChecksUtils.IncreaseOrAdd( channelWrapper.getRemoteAddress().getAddress() );
        if ( state == CheckState.CAPTCHA_ON_POSITION_FAILED )
        {
            PacketUtils.spawnPlayer( channelWrapper.getHandle(), userConnection.getPendingConnection().getVersion(), false );
            channelWrapper.getHandle().write( PacketUtils.checkMessage.get( version ) );
            channelWrapper.getHandle().flush();
        } else
        {
            PacketUtils.spawnPlayer( channelWrapper.getHandle(), userConnection.getPendingConnection().getVersion(), state == CheckState.ONLY_CAPTCHA );
            channelWrapper.getHandle().write( PacketUtils.captchaCheckMessage.get( version ) );
            sendCaptcha();
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
            if ( state == CheckState.CAPTCHA_POSITION && aticks < TOTAL_TICKS )
            {
                channelWrapper.getHandle().writeAndFlush( PacketUtils.resetSlot.get( version ) );
                state = CheckState.ONLY_POSITION;
            } else
            {
                state = CheckState.FAILED;
                PacketUtils.kickPlayer( PacketUtils.KickType.NOTPLAYER, Protocol.GAME, channelWrapper, version );
                BungeeCord.getInstance().getLogger().log( Level.INFO, "[{0}] disconnected: Too fast check passed", name );
            }
            return;
        }

        KickType type = BotFilter.getInstance().checkIpAddress( channelWrapper.getRemoteAddress().getAddress(),
                (int) ( totalping / ( lastSend == 0 ? sentPings : sentPings - 1 ) ) );
        if ( type != null )
        {
            PacketUtils.kickPlayer( type, Protocol.GAME, channelWrapper, version );
            BungeeCord.getInstance().getLogger().log( Level.INFO, "[{0}] disconnected: {1}", new Object[]
            {
                name, ( type == KickType.PROXY ? "Proxy detected" : type == KickType.COUNTRY ? "Country is not allowed" : "Big ping" )
            } );
            return;
        }

        channelWrapper.getHandle().writeAndFlush( PacketUtils.checkSus.get( version ) );
        BotFilter.getInstance().saveUser( getName(), channelWrapper.getRemoteAddress().getAddress() );
        BotFilter.getInstance().removeConnection( null, this );
        userConnection.setNeedLogin( false );
        userConnection.getPendingConnection().finishLogin( userConnection );
        disconnected( false );
    }

    @Override
    public void onMove()
    {
        if ( lastY == -1 || state == CheckState.FAILED || state == CheckState.SUCCESSFULLY || onGround )
        {
            return;
        }
        if ( state == CheckState.ONLY_CAPTCHA )
        {
            if ( lastY != y && waitingTeleportId == -1 )
            {
                resetPosition( true );
            }
            return;
        }
        if ( formatDouble( lastY - y ) != getSpeed( ticks ) )
        {
            if ( state == CheckState.CAPTCHA_ON_POSITION_FAILED )
            {
                state = CheckState.ONLY_CAPTCHA;
                joinTime = System.currentTimeMillis();
                resetPosition( true );
                sendCaptcha();
            } else
            {
                state = CheckState.FAILED;
                PacketUtils.kickPlayer( PacketUtils.KickType.NOTPLAYER, Protocol.GAME, channelWrapper, userConnection.getPendingConnection().getVersion() );
                BungeeCord.getInstance().getLogger().log( Level.INFO, "[{0}] disconnected: Failed position check", name );
            }
            return;
        }
        if ( y <= 60 && state == CheckState.CAPTCHA_POSITION )
        {
            resetPosition( false );
        }
        ticks++;
        if ( aticks >= TOTAL_TICKS && state != CheckState.CAPTCHA_POSITION )
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
            channelWrapper.getHandle().write( PacketUtils.singlePackets.get( PlayerAbilities.class ).get( version ) );
        }
        waitingTeleportId = 9876;
        channelWrapper.getHandle().writeAndFlush( PacketUtils.singlePackets.get( PlayerPositionAndLook.class ).get( version ) );
    }

    @Override
    public void handle(Chat chat) throws Exception
    {
        String message = chat.getMessage();
        if ( message.length() > 256 )
        {
            PacketUtils.kickPlayer( KickType.NOTPLAYER, Protocol.GAME, channelWrapper, version );
            BungeeCord.getInstance().getLogger().log( Level.INFO, "[{0}] disconnected: Too long message", name );
            return;
        }
        if ( message.length() > 4 )
        {
            --attemps;
            channelWrapper.getHandle().write( PacketUtils.captchaFailedMessage[attemps - 1].get( version ) );
            sendCaptcha();
        } else if ( message.replace( "/", "" ).equals( String.valueOf( captchaAnswer ) ) )
        {
            completeCheck();
        } else if ( --attemps != 0 )
        {
            channelWrapper.getHandle().write( PacketUtils.captchaFailedMessage[attemps - 1].get( version ) );
            sendCaptcha();
        } else
        {
            state = CheckState.FAILED;
            PacketUtils.kickPlayer( KickType.NOTPLAYER, Protocol.GAME, channelWrapper, version );
            BungeeCord.getInstance().getLogger().log( Level.INFO, "[{0}] disconnected: Failed captcha check", name );
        }
    }

    @Override
    public void handle(KeepAlive keepAlive) throws Exception
    {
        if ( keepAlive.getRandomId() == 9876 )
        {
            if ( lastSend == 0 )
            {
                state = CheckState.FAILED;
                PacketUtils.kickPlayer( KickType.NOTPLAYER, Protocol.GAME, channelWrapper, version );
                BungeeCord.getInstance().getLogger().log( Level.INFO, "[{0}] disconnected: Tried send fake ping", name );
                return;
            }
            if ( totalping == 9999 )
            {
                totalping = ( System.currentTimeMillis() - lastSend );
            } else
            {
                totalping = +( System.currentTimeMillis() - lastSend );
            }
            lastSend = 0;
        }
    }

    public void sendPing()
    {
        if ( this.lastSend == 0 && !( state == CheckState.FAILED || state == CheckState.SUCCESSFULLY ) )
        {
            lastSend = System.currentTimeMillis();
            sentPings++;
            channelWrapper.getHandle().writeAndFlush( PacketUtils.singlePackets.get( KeepAlive.class ).get( version ) );
        }
    }

    private void sendCaptcha()
    {
        captchaAnswer = random.nextInt( 100, 999 );
        channelWrapper.getHandle().write( PacketUtils.singlePackets.get( SetSlot.class ).get( version ) );
        channelWrapper.getHandle().writeAndFlush( PacketUtils.captchas.get( version, captchaAnswer ) );
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
