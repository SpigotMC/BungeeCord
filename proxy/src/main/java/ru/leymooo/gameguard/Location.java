package ru.leymooo.gameguard;

import ru.leymooo.gameguard.utils.NumberConversions;
import ru.leymooo.gameguard.utils.Vector;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.extra.Player;
import net.md_5.bungee.protocol.packet.extra.PlayerLook;
import net.md_5.bungee.protocol.packet.extra.PlayerPosition;
import net.md_5.bungee.protocol.packet.extra.PlayerPositionAndLook;

/**
 *
 * @author Leymooo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = "lastY")
public class Location implements Cloneable
{

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private boolean onGround;
    private double lastY;

    public void handlePosition(DefinedPacket packet)
    {
        if ( packet instanceof Player )
        {
            setOnGround( ( (Player) packet ).isOnGround() );
        } else if ( packet instanceof PlayerPosition )
        {
            PlayerPosition playerPos = (PlayerPosition) packet;
            setX( playerPos.getX() );
            setLastY( getY() );
            setY( playerPos.getY() );
            setZ( playerPos.getZ() );
            setOnGround( playerPos.isOnGround() );
        } else if ( packet instanceof PlayerPositionAndLook )
        {
            PlayerPositionAndLook playerPos = (PlayerPositionAndLook) packet;
            setX( playerPos.getX() );
            setLastY( getY() );
            setY( playerPos.getY() );
            setZ( playerPos.getZ() );
            setYaw( playerPos.getYaw() );
            setPitch( playerPos.getPitch() );
            setOnGround( playerPos.isOnGround() );
        } else if ( packet instanceof PlayerLook )
        {
            PlayerLook look = (PlayerLook) packet;
            setYaw( look.getYaw() );
            setPitch( look.getPitch() );
            setOnGround( look.isOnGround() );
        }
    }

    public static Location LocationFromLong(long location)
    {
        double x = ( location >> 38 );
        double y = ( location << 26 >> 52 );
        double z = ( location << 38 >> 38 );
        return new Location( x, y, z, 0, 0, false, 0 );

    }

    public Vector getDirection()
    {
        Vector vector = new Vector();

        double rotX = this.getYaw();
        double rotY = this.getPitch();

        vector.setY( -Math.sin( Math.toRadians( rotY ) ) );

        double xz = Math.cos( Math.toRadians( rotY ) );

        vector.setX( -xz * Math.sin( Math.toRadians( rotX ) ) );
        vector.setZ( xz * Math.cos( Math.toRadians( rotX ) ) );

        return vector;
    }

    public Location add(double x, double y, double z, float yaw, float pitch)
    {
        this.x += x;
        this.y += y;
        this.z += z;
        this.yaw += yaw;
        this.pitch += pitch;
        return this;
    }

    public Location subtract(Location vec)
    {
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
        return this;
    }

    public Vector toVector()
    {
        return new Vector( x, y, z );
    }

    public double distance(Location o)
    {
        return Math.sqrt( distanceSquared( o ) );
    }

    public double distanceSquared(Location o)
    {
        return NumberConversions.square( x - o.x ) + NumberConversions.square( y - o.y ) + NumberConversions.square( z - o.z );
    }

    @Override
    public Location clone()
    {
        try
        {
            return (Location) super.clone();
        } catch ( CloneNotSupportedException e )
        {
            throw new Error( e );
        }
    }

}
