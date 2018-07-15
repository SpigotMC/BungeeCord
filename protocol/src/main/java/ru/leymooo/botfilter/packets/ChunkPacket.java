package ru.leymooo.botfilter.packets;

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
@EqualsAndHashCode(callSuper = false, of =
{
    "x", "z"
})
public class ChunkPacket extends DefinedPacket
{

    int x;
    int z;
    byte[] data;
    boolean unload;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int version)
    {
        this.x = buf.readInt();
        this.z = buf.readInt();
        buf.readBoolean();
        readVarInt( buf );
        byte[] data = new byte[ readVarInt( buf ) ];
        buf.readBytes( data );
        buf.skipBytes( buf.readableBytes() );

        if ( x == 0 && z == 0 )
        {
            System.out.println( data.length );
            System.out.println( Arrays.toString( data ) );
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int version)
    {
        buf.writeInt( this.x );
        buf.writeInt( this.z );
        buf.writeBoolean( true );
        if ( version == ProtocolConstants.MINECRAFT_1_8 )
        {
            buf.writeShort( unload ? 0 : 1 );
        } else
        {
            writeVarInt( 0, buf );
        }
        writeArray( version >= ProtocolConstants.MINECRAFT_1_13 ? createDataFor113() : this.data, buf );
        if ( version >= ProtocolConstants.MINECRAFT_1_9_4 )
        {
            DefinedPacket.writeVarInt( 0, buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
    }

    private byte[] createDataFor113()
    {
        byte[] result = new byte[ 1024 ];
        Arrays.fill( data, (byte)0 );
        
        for ( int i = 3; i < 1024; i+=4 )
        {
            result[i] = 34;
        }
        
        //System.out.println( Arrays.toString( result) );
        return result;
    }

}
