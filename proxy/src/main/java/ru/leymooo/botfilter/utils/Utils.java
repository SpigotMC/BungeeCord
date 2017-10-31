package ru.leymooo.botfilter.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.packet.extra.Animation;
import net.md_5.bungee.protocol.packet.extra.UpdateHeath;
import ru.leymooo.botfilter.Config;
import ru.leymooo.botfilter.BFConnector;
import ru.leymooo.botfilter.Location;

/**
 *
 * @author Leymooo
 */
public class Utils
{

    private static Animation DAMAGE_PACKET = new Animation( -1, 1 );
    private static Animation SWING_PACKET = new Animation( -1, 0 );

    public static Cache<String, Integer> connections = CacheBuilder.newBuilder()
            .concurrencyLevel( Runtime.getRuntime().availableProcessors() )
            .initialCapacity( 100 )
            .expireAfterWrite( 10, TimeUnit.MINUTES )
            .build();

    public static boolean isManyChecks(String ip, boolean add, boolean check)
    {
        Integer conns = connections.getIfPresent( ip );
        if ( conns != null && conns >= 3 )
        {
            Config.getConfig().getProxy().addProxyForce( ip );
            return true;
        }
        if ( check )
        {
            return conns >= 2;
        }
        if ( add )
        {
            connections.put( ip, conns == null ? 1 : conns + 1 );
        }
        return false;
    }

    public static boolean disconnect(BFConnector connector)
    {
        UserConnection connection = connector.getConnection();
        long pingAvg = connector.getGlobalPing() / ( connector.getPingChecks()== 0 ? 1 : connector.getPingChecks() );
        String ip = connection.getAddress().getAddress().getHostAddress();
        Config config = Config.getConfig();
        GeoIpUtils geo = config.getGeoUtils();
        boolean proxy = config.getProxy().isProxy( ip );
        if ( !config.isProtectionEnabled() )
        {
            return false;
        }
        if ( proxy || !geo.isAllowed( geo.getCountryCode( connector.getConnection().getAddress().getAddress() ), config.isPermanent() ) )
        {
            connection.disconnect( proxy ? config.getErrorProxy() : config.getErrorConutry() );
            return true;
        }
        if ( !( connector.isClientSettings() && connector.isPluginMessage()
                && connector.getChecks() != null && connector.getChecks().isEmpty() ) )
        {
            connection.disconnect( config.getErrorBot() );
            return true;
        }
        //                                     Ну хотябы 2 раза клиент то должен должен ответить на пинг.
        if ( ( pingAvg > config.getMaxPing() || connector.getPingChecks() <= 1 ) && ( config.getMaxPing() != -1 ) )
        {
            connection.disconnect( config.getBigPing() );
            return true;
        }
        return false;
    }

    public static double getFallSpeed(int localTick)
    {
        return formatDouble( Math.abs( ( Math.pow( 0.98, localTick ) - 1 ) * 3.92 ) );
    }

    public static double formatDouble(double d)
    {
        return Math.floor( d * 100 ) / 100;
    }

    public static void sendPackets(BFConnector connector)
    {
        int globalTick = connector.getGlobalTick();
        if ( globalTick >= 7 && globalTick <= 50 && globalTick % 6 == 0 )
        {
            connector.write( connector.getHeldItemSlot().increase() );
            connector.addOrRemove( connector.getHeldItemSlot().getSlot(), false );
            connector.write( connector.getSetSlotPacket().updateSlotAndData() );
        }
        UpdateHeath healthPacket = connector.getHealthPacket();
        if ( healthPacket.getHealth() == 20 )
        {
            connector.write( SWING_PACKET );
            connector.addOrRemove( SWING_PACKET, false );
            connector.write( DAMAGE_PACKET );
        }
        connector.write( connector.getSetExpPacket().increase() );
        connector.write( healthPacket.increase() );
        connector.getChannel().flush();
    }

    public static boolean checkPps(BFConnector connector)
    {
        AtomicInteger packets = connector.getPps();
        if ( packets == null )
        {
            connector.setPps( packets = new AtomicInteger() );
        }
        if ( System.currentTimeMillis() - connector.getLastPpsCheck() <= 1000 )
        {
            if ( packets.incrementAndGet() >= 55 )
            {
                connector.getConnection().disconnect( Config.getConfig().getErrorPackets() );
                connector.setState( CheckState.FAILED );
                return true;
            }
            return false;
        }
        packets.set( 0 );
        connector.setLastPpsCheck( System.currentTimeMillis() );
        return false;
    }

    public static boolean canUseButton(Location playerLoc, Location blockLoc)
    {
        Vector toBlock = blockLoc.toVector().subtract( playerLoc.clone().add( 0, 1.62, 0, 0, 0 ).toVector() );
        Vector direction = playerLoc.getDirection();
        double distance = playerLoc.distance( blockLoc );
        double dot = toBlock.normalize().dot( direction );
        return ( 0.35 < dot && dot <= 1 ) && ( distance <= 4.95 && distance > 1.5 ) && playerLoc.isOnGround() && playerLoc.getY() == blockLoc.getY() - 1;
    }

    public static enum CheckState
    {
        POSITION,
        BUTTON,
        SUS,
        FAILED;
    }
}
