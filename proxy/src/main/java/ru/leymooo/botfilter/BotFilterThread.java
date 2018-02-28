package ru.leymooo.botfilter;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.protocol.Protocol;
import ru.leymooo.botfilter.caching.PacketUtil;
import ru.leymooo.botfilter.caching.PacketUtil.KickType;
import ru.leymooo.botfilter.config.Settings;

/**
 *
 * @author Leymooo
 */
public class BotFilterThread
{

    private static Thread thread;

    public static void start()
    {
        ( thread = new Thread( () ->
        {
            while ( !Thread.interrupted() && sleep() )
            {
                try
                {
                    long currTime = System.currentTimeMillis();
                    for ( Connector connector : BotFilter.getInstance().connectedUsersSet.values() )
                    {
                        if ( !connector.isConnected() )
                        {
                            System.out.println( "this" );
                            continue;
                        }
                        System.out.println( "this1" );
                        BotFilter.CheckState state = connector.state;
                        switch ( state )
                        {
                            case SUCCESSFULLY:
                            case FAILED:
                                continue;
                            default:
                                if ( ( currTime - connector.joinTime ) >= Settings.IMP.TIME_OUT )
                                {
                                    connector.state = BotFilter.CheckState.FAILED;
                                    PacketUtil.kickPlayer( KickType.NOTPLAYER, Protocol.GAME, connector.channelWrapper, connector.version );
                                } else if ( state == BotFilter.CheckState.ONLY_POSITION || state == BotFilter.CheckState.CAPTCHA_ON_POSITION_FAILED )
                                {
                                    connector.channelWrapper.getHandle().writeAndFlush( PacketUtil.checkMessage.get( connector.version ) );
                                } else
                                {
                                    connector.channelWrapper.getHandle().writeAndFlush( PacketUtil.captchaCheckMessage.get( connector.version ) );
                                }
                        }
                    }
                } catch ( Exception e )
                {
                    BungeeCord.getInstance().getLogger().log( Level.WARNING, "[BotFilter] Непонятная ошибка. Пожалуйста отправте ёё разработчику!", e );
                }
            }

        }, "BotFilter thread" ) ).start();
    }

    public static void stop()
    {
        if ( thread != null && thread.isAlive() )
        {
            thread.interrupt();
        }
    }

    private static boolean sleep()
    {
        try
        {
            Thread.sleep( 750 );
        } catch ( InterruptedException ex )
        {
            return false;
        }
        return true;
    }
}
