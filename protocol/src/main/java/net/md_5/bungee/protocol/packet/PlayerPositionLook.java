package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlayerPositionLook extends DefinedPacket
{

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private byte flags;
    private int teleportID;
    private boolean onGround;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        if ( direction == ProtocolConstants.Direction.TO_CLIENT )
        {
            flags = buf.readByte();
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
            {
                teleportID = readVarInt( buf );
            }
        } else
        {
            onGround = buf.readBoolean();
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.writeDouble( x );
        buf.writeDouble( y );
        buf.writeDouble( z );
        buf.writeFloat( yaw );
        buf.writeFloat( pitch );
        if ( direction == ProtocolConstants.Direction.TO_CLIENT )
        {
            buf.writeByte( flags );
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
            {
                writeVarInt( teleportID, buf );
            }
        } else
        {
            buf.writeBoolean( onGround );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
