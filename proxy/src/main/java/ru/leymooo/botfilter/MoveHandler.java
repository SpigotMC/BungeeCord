package ru.leymooo.botfilter;

import net.md_5.bungee.netty.PacketHandler;
import ru.leymooo.botfilter.packets.Player;
import ru.leymooo.botfilter.packets.PlayerPosition;
import ru.leymooo.botfilter.packets.PlayerPositionAndLook;
import ru.leymooo.botfilter.packets.TeleportConfirm;

/**
 * @author Leymooo
 */
public class MoveHandler extends PacketHandler
{

    public double x = 0;
    public double y = 0;
    public double z = 0;
    public boolean onGround = false;

    public int teleportId = -1;

    public int waitingTeleportId = 9876;

    public double lastY = 0;
    public int ticks = 0;

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
        onGround = pos.isOnGround();
        onMove();
    }

    @Override
    public void handle(PlayerPositionAndLook posRot) throws Exception
    {
        if ( ( (Connector) this ).getVersion() == 47 && posRot.getX() == 7 && posRot.getY() == 450 && posRot.getZ() == 7 && waitingTeleportId == 9876 )
        {
            ticks = 0;
            y = -1;
            lastY = -1;
            waitingTeleportId = -1;
        }
        x = posRot.getX();
        lastY = y;
        y = posRot.getY();
        z = posRot.getZ();
        onGround = posRot.isOnGround();
        onMove();
    }

    @Override
    public void handle(TeleportConfirm confirm) throws Exception
    {
        if ( confirm.getTeleportId() == waitingTeleportId )
        {
            ticks = 0;
            y = -1;
            lastY = -1;
            waitingTeleportId = -1;
        }
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
