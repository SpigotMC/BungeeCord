package ru.leymooo.botfilter.packets;

import io.netty.buffer.ByteBuf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MapDataPacket extends DefinedPacket
{

    private int mapId;
    private byte scale;
    private MapData data;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_8 )
        {
            MapDataPacket.writeVarInt( this.mapId, buf );

            buf.writeByte( this.scale );
            MapDataPacket.writeVarInt( 0, buf );
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
            {
                buf.writeBoolean( false );
            }
            buf.writeByte( data.getColumns() );
            buf.writeByte( data.getRows() );
            buf.writeByte( data.getX() );
            buf.writeByte( data.getY() );
            MapDataPacket.writeVarInt( data.getData().length, buf );
            buf.writeBytes( data.getData() );
        } else //1.7
        {
            byte[] data = new byte[ this.data.getData().length + 3 ];
            data[0] = 0;
            data[1] = (byte) this.data.getX();
            data[2] = (byte) this.data.getY();
            System.arraycopy( this.data.getData(), 0, data, 3, data.length - 3 );
            buf.writeShort( data.length );
            buf.writeBytes( data );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
    }

    @AllArgsConstructor
    @Getter
    public static class MapData
    {

        private int columns;
        private int rows;
        private int x;
        private int y;
        private byte[] data;
    }

}
