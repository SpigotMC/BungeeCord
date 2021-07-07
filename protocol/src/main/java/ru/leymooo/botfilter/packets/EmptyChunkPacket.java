package ru.leymooo.botfilter.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import java.io.IOException;
import java.util.BitSet;
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


        if ( version < ProtocolConstants.MINECRAFT_1_17 )
        {
            buf.writeBoolean( true );
        }

        if ( version >= ProtocolConstants.MINECRAFT_1_16 && version < ProtocolConstants.MINECRAFT_1_16_2 )
        {
            buf.writeBoolean( true );
        }

        //BitMasks
        if ( version < ProtocolConstants.MINECRAFT_1_17 )
        {
            if ( version == ProtocolConstants.MINECRAFT_1_8 )
            {
                buf.writeShort( 1 );
            } else
            {
                writeVarInt( 0, buf );
            }
        } else
        {
            BitSet bitSet = new BitSet();
            for ( int i = 0; i < 16; i++ )
            {
                bitSet.set( i, false );
            }
            long[] mask = bitSet.toLongArray();
            DefinedPacket.writeVarInt( mask.length, buf );
            for ( long l : mask )
            {
                buf.writeLong( l );
            }
        }
        if ( version >= ProtocolConstants.MINECRAFT_1_14 )
        {
            this.write1_14Heightmaps( buf );
            if ( version >= ProtocolConstants.MINECRAFT_1_15 )
            {
                if ( version >= ProtocolConstants.MINECRAFT_1_16_2 )
                {
                    writeVarInt( 1024, buf );
                    for ( int i = 0; i < 1024; i++ )
                    {
                        writeVarInt( 1, buf );
                    }
                } else
                {
                    for ( int i = 0; i < 1024; i++ )
                    {
                        buf.writeInt( 0 );
                    }
                }
            }
        }
        if ( version < ProtocolConstants.MINECRAFT_1_13 )
        {
            writeArray( new byte[ 256 ], buf ); //1.8 - 1.12.2
        } else if ( version == ProtocolConstants.MINECRAFT_1_13 )
        {
            writeArray( new byte[ 512 ], buf ); //1.13
        } else
        {
            writeArray( new byte[ 1024 ], buf ); //1.13.1 - 1.xx
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

    private void write1_14Heightmaps(ByteBuf buf)
    {
        try ( ByteBufOutputStream output = new ByteBufOutputStream( buf ) )
        {
            output.writeByte( 10 ); //CompoundTag
            output.writeUTF( "" ); // CompoundName
            output.writeByte( 10 ); //CompoundTag
            output.writeUTF( "root" ); //root compound
            output.writeByte( 12 ); //long array
            output.writeUTF( "MOTION_BLOCKING" );
            long[] longArrayTag = new long[36];
            output.writeInt( longArrayTag.length );
            for ( int i = 0, length = longArrayTag.length; i < length; i++ )
            {
                output.writeLong( longArrayTag[i] );
            }
            buf.writeByte( 0 ); //end of compound
            buf.writeByte( 0 ); //end of compound
        } catch ( IOException ex )
        {
            throw new RuntimeException( ex );
        }
    }
}
