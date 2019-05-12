package ru.leymooo.botfilter.packets;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.CompoundTagBuilder;
import com.sk89q.jnbt.NBTOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import java.io.IOException;
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

    int x;
    int z;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int version)
    {
        buf.writeInt( this.x );
        buf.writeInt( this.z );
        buf.writeBoolean( true );

        //Damn you, 1.14
        if ( version >= ProtocolConstants.MINECRAFT_1_14 )
        {
            try
            {
                writeVarInt( 0, buf );

                CompoundTag tag = CompoundTagBuilder.create()
                        .putLongArray( "MOTION_BLOCKING", new long[ roundToNearest( 256 * 9, 64 ) / 64 ] )
                        .build(); //1.14 - heightmaps, important thing

                new NBTOutputStream( new ByteBufOutputStream( buf ) ).writeNamedTag( "root", tag );

                writeArray( new byte[ 1024 ], buf );
                writeVarInt( 0, buf );
            } catch ( IOException e )
            {
                throw new RuntimeException( "Cannot write NBT tag to buffer.", e );
            }
            return;
        }

        if ( version == ProtocolConstants.MINECRAFT_1_8 )
        {
            buf.writeShort( 1 );
        } else
        {
            writeVarInt( 0, buf );
        }
        if ( version < ProtocolConstants.MINECRAFT_1_13 )
        {
            writeArray( new byte[ 256 ], buf ); //1.8 - 1.12.2
        } else if ( version == ProtocolConstants.MINECRAFT_1_13 )
        {
            writeArray( new byte[ 512 ], buf ); //1.13
        } else
        {
            writeArray( new byte[ 1024 ], buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
    }

    private static int roundToNearest(int value, int roundTo)
    {
        if ( roundTo == 0 )
        {
            return 0;
        } else if ( value == 0 )
        {
            return roundTo;
        } else
        {
            if ( value < 0 )
            {
                roundTo *= -1;
            }

            int remainder = value % roundTo;
            return remainder != 0 ? value + roundTo - remainder : value;
        }
    }

}
