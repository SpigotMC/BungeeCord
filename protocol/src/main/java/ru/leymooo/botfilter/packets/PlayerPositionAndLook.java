package ru.leymooo.botfilter.packets;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude =
    {
        "onGround", "teleportId"
    })
public class PlayerPositionAndLook extends DefinedPacket
{

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private int teleportId;
    private boolean onGround;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.writeDouble( this.x );
        buf.writeDouble( this.y );
        buf.writeDouble( this.z );
        buf.writeFloat( this.yaw );
        buf.writeFloat( this.pitch );
        buf.writeByte( 0x00 );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
        {
            PlayerPositionAndLook.writeVarInt( teleportId, buf );
        }
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.yaw = buf.readFloat();
        this.pitch = buf.readFloat();
        this.onGround = buf.readBoolean();
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
