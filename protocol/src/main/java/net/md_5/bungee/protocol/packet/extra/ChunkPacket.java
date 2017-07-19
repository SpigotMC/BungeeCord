package net.md_5.bungee.protocol.packet.extra;

import io.netty.buffer.ByteBuf;
import java.util.Arrays;
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
@EqualsAndHashCode(callSuper = false)
public class ChunkPacket extends DefinedPacket
{

    int x;
    int z;
    byte[] data;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int version)
    {
        buf.writeInt( this.x );
        buf.writeInt( this.z );
        buf.writeBoolean( true );
        if ( version > 47 )
        {
            DefinedPacket.writeVarInt( 0, buf );
        } else
        {
            buf.writeShort( 1 );
        }
        DefinedPacket.writeVarInt( 256, buf );
        Arrays.fill( this.data, (byte) 18 );
        buf.writeBytes( this.data );
        if ( version >= 110 )
        {
            DefinedPacket.writeVarInt( 0, buf );
        }
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.skipBytes( buf.readableBytes() );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
    }

}
