package net.md_5.bungee.protocol.packet.extra;

import io.netty.buffer.ByteBuf;
import java.beans.ConstructorProperties;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

public class SpawnPosition
        extends DefinedPacket
{

    private int x;
    private int y;
    private int z;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        //buf.writeLong(this.x<<38| this.y<<26| this.z);
        buf.writeLong( 1378416066565L );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        throw new UnsupportedOperationException();
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public int getZ()
    {
        return this.z;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public void setZ(int z)
    {
        this.z = z;
    }

    @Override
    public String toString()
    {
        return "SpawnPosition(x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + ")";
    }

    @ConstructorProperties(value =
    {
        "x", "y", "z"
    })
    public SpawnPosition(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o)
    {
        if ( o == this )
        {
            return true;
        }
        if ( !( o instanceof SpawnPosition ) )
        {
            return false;
        }
        SpawnPosition other = (SpawnPosition) o;
        if ( !other.canEqual( this ) )
        {
            return false;
        }
        if ( this.getX() != other.getX() )
        {
            return false;
        }
        if ( this.getY() != other.getY() )
        {
            return false;
        }
        if ( this.getZ() != other.getZ() )
        {
            return false;
        }
        return true;
    }

    protected boolean canEqual(Object other)
    {
        return other instanceof SpawnPosition;
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = result * 59 + this.getX();
        result = result * 59 + this.getY();
        result = result * 59 + this.getZ();
        return result;
    }
}
