package ru.leymooo.botfilter;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.PluginMessage;
import ru.leymooo.botfilter.BotFilter.CheckState;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.caching.PacketUtils.KickType;
import ru.leymooo.botfilter.caching.PacketsPosition;
import ru.leymooo.botfilter.config.Settings;
import ru.leymooo.botfilter.utils.FailedUtils;
import ru.leymooo.botfilter.utils.IPUtils;
import ru.leymooo.botfilter.utils.ManyChecksUtils;

/**
 * @author Leymooo
 */
@EqualsAndHashCode(callSuper = false, of =
    {
        "name"
    })
public class Connector extends MoveHandler
{

    private static final Logger LOGGER = BungeeCord.getInstance().getLogger();

    public static int TOTAL_TICKS = 100;
    private static long TOTAL_TIME = ( TOTAL_TICKS * 50 ) - 100; //TICKS * 50MS

    private final BotFilter botFilter;
    @Getter
    private UserConnection userConnection;

    private final String name;
    private final String ip;
    @Getter
    private final int version;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    @Getter
    @Setter
    private CheckState state = CheckState.CAPTCHA_ON_POSITION_FAILED;
    @Getter
    private Channel channel;
    private int aticks = 0, sentPings = 0, captchaAnswer, attemps = 3;
    @Getter
    private long joinTime = System.currentTimeMillis();
    private long lastSend = 0, totalping = 9999;
    private boolean markDisconnected = false;

    public Connector(UserConnection userConnection, BotFilter botFilter)
    {
        this.botFilter = botFilter;
        this.state = this.botFilter.getCurrentCheckState();
        this.name = userConnection.getName();
        this.channel = userConnection.getCh().getHandle();
        this.userConnection = userConnection;
        this.version = userConnection.getPendingConnection().getVersion();
        this.userConnection.setClientEntityId( PacketUtils.CLIENTID );
        this.userConnection.setDimension( 0 );
        this.ip = IPUtils.getAddress( this.userConnection ).getHostAddress();
    }


    public void spawn()
    {
        this.botFilter.incrementBotCounter();
        if ( !Settings.IMP.PROTECTION.ALWAYS_CHECK )
        {
            ManyChecksUtils.IncreaseOrAdd( IPUtils.getAddress( this.userConnection ) );
        }
        if ( state == CheckState.CAPTCHA_ON_POSITION_FAILED )
        {
            PacketUtils.spawnPlayer( channel, userConnection.getPendingConnection().getVersion(), false, false );
            PacketUtils.titles[0].writeTitle( channel, version );
        } else
        {
            PacketUtils.spawnPlayer( channel, userConnection.getPendingConnection().getVersion(), state == CheckState.ONLY_CAPTCHA, true );
            sendCaptcha();
            PacketUtils.titles[1].writeTitle( channel, version );
        }
        sendPing();
        //this.botFilter.addConnection( this );
        LOGGER.log( Level.INFO, toString() + " has connected" );
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        markDisconnected = true;
        if ( state == CheckState.FAILED )
        {
            channel.close();
        } else
        {
            this.userConnection.disconnect( Util.exception( t ) );
        }
        disconnected();
    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        switch ( state )
        {
            case ONLY_CAPTCHA:
            case ONLY_POSITION:
            case CAPTCHA_POSITION:
                String info = "(BF) [" + name + "|" + ip + "] leaved from server during check";
                LOGGER.log( Level.INFO, info );
                FailedUtils.addIpToQueue( ip, KickType.LEAVED );
                break;
        }
        botFilter.removeConnection( null, this );
        disconnected();
    }

    @Override
    public void handlerChanged()
    {
        disconnected();
    }

    private void disconnected()
    {
        channel = null;
        userConnection = null;
    }

    public void completeCheck()
    {
        if ( System.currentTimeMillis() - joinTime < TOTAL_TIME && state != CheckState.ONLY_CAPTCHA )
        {
            if ( state == CheckState.CAPTCHA_POSITION && aticks < TOTAL_TICKS )
            {
                channel.writeAndFlush( PacketUtils.getCachedPacket( PacketsPosition.SETSLOT_RESET ).get( version ), channel.voidPromise() );
                state = CheckState.ONLY_POSITION;
            } else
            {
                if ( state == CheckState.CAPTCHA_ON_POSITION_FAILED )
                {
                    changeStateToCaptcha();
                } else
                {
                    failed( KickType.FAILED_FALLING, "Too fast check passed" );
                }
            }
            return;
        }
        int devide = lastSend == 0 ? sentPings : sentPings - 1;
        if ( botFilter.checkBigPing( totalping / ( devide <= 0 ? 1 : devide ) ) )
        {
            failed( KickType.PING, "Big ping" );
            return;
        }
        state = CheckState.SUCCESSFULLY;
        PacketUtils.titles[2].writeTitle( channel, version );
        channel.flush();
        botFilter.removeConnection( null, this );
        sendMessage( PacketsPosition.CHECK_SUS );
        botFilter.saveUser( getName(), IPUtils.getAddress( userConnection ), true );
        userConnection.setNeedLogin( false );
        userConnection.getPendingConnection().finishLogin( userConnection, true );
        markDisconnected = true;
        LOGGER.log( Level.INFO, "[BotFilter] Игрок (" + name + "|" + ip + ") успешно прошёл проверку" );
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
        // System.out.println( "lastY=" + lastY + "; y=" + y + "; diff=" + formatDouble( lastY - y ) + "; need=" + getSpeed( ticks ) +"; ticks=" + ticks );
        if ( formatDouble( lastY - y ) != getSpeed( ticks ) )
        {
            if ( state == CheckState.CAPTCHA_ON_POSITION_FAILED )
            {
                changeStateToCaptcha();
            } else
            {
                failed( KickType.FAILED_FALLING, "Failed position check" );
            }
            return;
        }
        if ( y <= 60 && state == CheckState.CAPTCHA_POSITION && waitingTeleportId == -1 )
        {
            resetPosition( false );
        }
        if ( aticks >= TOTAL_TICKS && state != CheckState.CAPTCHA_POSITION )
        {
            completeCheck();
            return;
        }
        if ( state == CheckState.CAPTCHA_ON_POSITION_FAILED )
        {
            ByteBuf expBuf = PacketUtils.expPackets.get( aticks, version );
            if ( expBuf != null )
            {
                channel.writeAndFlush( expBuf, channel.voidPromise() );
            }
        }
        ticks++;
        aticks++;
    }

    private void resetPosition(boolean disableFall)
    {
        if ( disableFall )
        {
            channel.write( PacketUtils.getCachedPacket( PacketsPosition.PLAYERABILITIES ).get( version ), channel.voidPromise() );
        }
        waitingTeleportId = 9876;
        channel.writeAndFlush( PacketUtils.getCachedPacket( PacketsPosition.PLAYERPOSANDLOOK_CAPTCHA ).get( version ), channel.voidPromise() );
    }

    @Override
    public void handle(Chat chat) throws Exception
    {
        if ( state != CheckState.CAPTCHA_ON_POSITION_FAILED )
        {
            String message = chat.getMessage();
            if ( message.length() > 256 )
            {
                failed( KickType.FAILED_CAPTCHA, "Too long message" );
                return;
            }
            if ( message.replace( "/", "" ).equals( String.valueOf( captchaAnswer ) ) )
            {
                completeCheck();
            } else if ( --attemps != 0 )
            {
                ByteBuf buf = attemps == 2 ? PacketUtils.getCachedPacket( PacketsPosition.CAPTCHA_FAILED_2 ).get( version )
                        : PacketUtils.getCachedPacket( PacketsPosition.CAPTCHA_FAILED_1 ).get( version );
                if ( buf != null )
                {
                    channel.write( buf, channel.voidPromise() );
                }
                sendCaptcha();
            } else
            {
                failed( KickType.FAILED_CAPTCHA, "Failed captcha check" );
            }
        }
    }

    @Override
    public void handle(ClientSettings settings) throws Exception
    {
        this.userConnection.setSettings( settings );
        this.userConnection.setCallSettingsEvent( true );
    }

    @Override
    public void handle(KeepAlive keepAlive) throws Exception
    {
        if ( keepAlive.getRandomId() == PacketUtils.KEEPALIVE_ID )
        {
            if ( lastSend == 0 )
            {
                failed( KickType.PING, "Tried send fake ping" );
                return;
            }
            long ping = System.currentTimeMillis() - lastSend;
            totalping = totalping == 9999 ? ping : totalping + ping;
            lastSend = 0;
        }
    }

    @Override
    public void handle(PluginMessage pluginMessage) throws Exception
    {
        if ( PluginMessage.SHOULD_RELAY.apply( pluginMessage ) )
        {
            userConnection.getPendingConnection().getRelayMessages().add( pluginMessage );
        } else
        {
            userConnection.getDelayedPluginMessages().add( pluginMessage );
        }

    }

    public void sendPing()
    {
        if ( this.lastSend == 0 && !( state == CheckState.FAILED || state == CheckState.SUCCESSFULLY ) )
        {
            lastSend = System.currentTimeMillis();
            sentPings++;
            channel.writeAndFlush( PacketUtils.getCachedPacket( PacketsPosition.KEEPALIVE ).get( version ) );
        }
    }

    private void sendCaptcha()
    {
        captchaAnswer = random.nextInt( 100, 999 );
        channel.write( PacketUtils.getCachedPacket( PacketsPosition.SETSLOT_MAP ).get( version ), channel.voidPromise() );
        channel.writeAndFlush( PacketUtils.captchas.get( version, captchaAnswer ), channel.voidPromise() );
    }

    private void changeStateToCaptcha()
    {
        state = CheckState.ONLY_CAPTCHA;
        joinTime = System.currentTimeMillis() + 3500;
        channel.write( PacketUtils.getCachedPacket( PacketsPosition.SETEXP_RESET ).get( version ), channel.voidPromise() );
        PacketUtils.titles[1].writeTitle( channel, version );
        resetPosition( true );
        sendCaptcha();
    }

    public String getName()
    {
        return name.toLowerCase();
    }

    public boolean isConnected()
    {
        return userConnection != null && channel != null && !markDisconnected && userConnection.isConnected();
    }

    public void failed(KickType type, String kickMessage)
    {
        state = CheckState.FAILED;
        PacketUtils.kickPlayer( type, Protocol.GAME, userConnection.getCh(), version );
        markDisconnected = true;
        LOGGER.log( Level.INFO, "(BF) [" + name + "|" + ip + "] check failed: " + kickMessage );
        FailedUtils.addIpToQueue( ip, type );
    }

    public void sendMessage(int index)
    {
        ByteBuf buf = PacketUtils.getCachedPacket( index ).get( getVersion() );
        if ( buf != null )
        {
            getChannel().write( buf, getChannel().voidPromise() );
        }
    }


    @Override
    public String toString()
    {
        return "[" + name + "|" + ip + "] <-> BotFilter";
    }
}
