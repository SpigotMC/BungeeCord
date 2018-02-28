package ru.leymooo.botfilter;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.netty.PacketHandler;
import ru.leymooo.botfilter.packets.Player;
import ru.leymooo.botfilter.packets.PlayerLook;
import ru.leymooo.botfilter.packets.PlayerPosition;
import ru.leymooo.botfilter.packets.PlayerPositionAndLook;

/**
 *
 * @author Leymooo
 */
public class MoveHandler extends PacketHandler
{

    public double x = 0;
    public double y = -1;
    public double z = 0;
    public boolean onGround = false;

    public int teleportId = -1;

    public int waitingTeleportId = -1;

    public double lastY = -1;
    public int ticks = 1;

    @Override
    public String toString()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void handle(Player player) throws Exception
    {
        this.onGround = player.isOnGround();
    }

    @Override
    public void handle(PlayerPosition pos) throws Exception
    {
        x = pos.getX();
        lastY = y;
        y = pos.getY();
        z = pos.getZ();
        onMove();
    }

    @Override
    public void handle(PlayerPositionAndLook posRot) throws Exception
    {
        x = posRot.getX();
        lastY = y;
        y = posRot.getY();
        z = posRot.getZ();
        onMove();
    }

    public void onMove()
    {
        throw new UnsupportedOperationException( "Method is not overrided" );
    }

    public static double getSpeed(int ticks)
    {
        return formatDouble( -( ( Math.pow( 0.98, ticks ) - 1 ) * 3.92 ) );
    }

    public static double formatDouble(double d)
    {
        return Math.floor( d * 100 ) / 100;
    }
}
