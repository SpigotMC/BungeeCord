package ru.leymooo.botfilter;

import java.util.HashSet;
import java.util.logging.Level;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.protocol.Protocol;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.caching.PacketUtils.KickType;
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
                                    connector.state = BotFilter.CheckState.FAILED;
                                    PacketUtils.kickPlayer( KickType.NOTPLAYER, Protocol.GAME, connector.userConnection.getCh(), connector.version );
                                    BungeeCord.getInstance().getLogger().log( Level.INFO, "[{0}] disconnected: "
                                            .concat( connector.state == BotFilter.CheckState.CAPTCHA_ON_POSITION_FAILED
                                                    ? "Too long fall check" : "Captcha not entered" ), connector.name );
                                    connector.markDisconnected = true;
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
                    for ( String remove : toRemove )
                    {
                        BotFilter.getInstance().removeConnection( remove, null );
                    }
                    toRemove.clear();
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
            Thread.sleep( 750 );
        } catch ( InterruptedException ex )
        {
            return false;
        }
        return true;
    }
}
