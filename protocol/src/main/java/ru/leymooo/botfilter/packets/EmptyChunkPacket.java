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
@EqualsAndHashCode(callSuper = false, of =
{
    "x", "z"
})
public class EmptyChunkPacket extends DefinedPacket
{

    private static byte[] mc1_8 = new byte[ 32 ], mc1_13 = new byte[ 512 ], mc1_13_1 = new byte[ 1024 ];

    int x;
    int z;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int version)
    {
        buf.writeInt( this.x );
        buf.writeInt( this.z );
        buf.writeBoolean( true );
        if ( version == ProtocolConstants.MINECRAFT_1_8 )
        {
            buf.writeShort( 1 );
        } else
        {
            writeVarInt( 0, buf );
        }
        if ( version < ProtocolConstants.MINECRAFT_1_13 )
        {
            writeArray( mc1_8, buf ); //1.8 - 1.12.2
        } else if ( version >= ProtocolConstants.MINECRAFT_1_13_1 )
        {
            writeArray( mc1_13_1, buf ); //1.13.1 - 1.xx
        } else
        {
            writeArray( mc1_13, buf ); //1.13
        }
        if ( version >= ProtocolConstants.MINECRAFT_1_9_4 )
        {
            DefinedPacket.writeVarInt( 0, buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
    }
}
