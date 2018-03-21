package ru.leymooo.botfilter;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.protocol.Protocol;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.caching.PacketUtils.KickType;
import ru.leymooo.botfilter.config.Settings;
import ru.leymooo.botfilter.utils.ManyChecksUtils;

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
            while ( !Thread.currentThread().isInterrupted() && sleep() )
            {
                HashSet<String> toRemove = new HashSet<>();
                try
                {
                    long currTime = System.currentTimeMillis();
                    for ( Connector connector : BotFilter.getInstance().connectedUsersSet.values() )
                    {
                        if ( !connector.isConnected() )
                        {
                            toRemove.add( connector.getName() );
                            continue;
                        }
                        BotFilter.CheckState state = connector.state;
                        switch ( state )
                        {
                            case SUCCESSFULLY:
                            case FAILED:
                                toRemove.add( connector.getName() );
                                continue;
                            default:
                                if ( ( currTime - connector.joinTime ) >= Settings.IMP.TIME_OUT )
                                {
                                    connector.failed( KickType.NOTPLAYER, connector.state == BotFilter.CheckState.CAPTCHA_ON_POSITION_FAILED
                                            ? "Too long fall check" : "Captcha not entered" );
                                    continue;
                                } else if ( state == BotFilter.CheckState.CAPTCHA_ON_POSITION_FAILED || state == BotFilter.CheckState.ONLY_POSITION )
                                {
                                    connector.channel.writeAndFlush( PacketUtils.packets[11].get( connector.version ) );
                                } else
                                {
                                    connector.channel.writeAndFlush( PacketUtils.packets[12].get( connector.version ) );
                                }
                                connector.sendPing();
                        }
                    }

                } catch ( Exception e )
                {
                    BungeeCord.getInstance().getLogger().log( Level.WARNING, "[BotFilter] Непонятная ошибка. Пожалуйста отправте ёё разработчику!", e );
                } finally
                {
                    if ( !toRemove.isEmpty() )
                    {
                        for ( String remove : toRemove )
                        {
                            BotFilter.getInstance().removeConnection( remove, null );
                        }
                        toRemove.clear();
                    }
                    toRemove = null;
                }
            }

        }, "BotFilter thread" ) ).start();
    }

    public static void stop()
    {
        if ( thread != null )
        {
            thread.interrupt();
        }
    }

    private static boolean sleep()
    {
        try
        {
            Thread.sleep( 1000 );
        } catch ( InterruptedException ex )
        {
            return false;
        }
        return true;
    }

    public static void startCleanUpThread()
    {
        new Thread( () ->
        {
            while ( !Thread.interrupted() )
            {
                ManyChecksUtils.cleanUP();
                if ( BungeeCord.getInstance().getConnectionThrottle() != null )
                {
                    BungeeCord.getInstance().getConnectionThrottle().cleanUP();
                }
                if ( BotFilter.getInstance() != null && BotFilter.getInstance().getServerPingUtils() != null )
                {
                    BotFilter.getInstance().getServerPingUtils().cleanUP();
                }
                try
                {
                    Thread.sleep( 60000L );
                } catch ( InterruptedException ex )
                {
                    return;
                }
            }
        }, "CleanUp thread" ).start();

    }
}
