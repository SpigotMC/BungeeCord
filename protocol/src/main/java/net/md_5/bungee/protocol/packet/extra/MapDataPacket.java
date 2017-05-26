package net.md_5.bungee.protocol.packet.extra;

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
        MapDataPacket.writeVarInt( this.mapId, buf );

        buf.writeByte( this.scale );
        MapDataPacket.writeVarInt( 0, buf );
        if ( protocolVersion >= 107 )
        {
            buf.writeBoolean( false );
        }
        MapDataNew column = (MapDataNew) this.data;
        buf.writeByte( column.getColumns() );
        if ( column.getColumns() <= 0 )
        {
            return;
        }
        buf.writeByte( column.getRows() );
        buf.writeByte( column.getX() );
        buf.writeByte( column.getY() );
        MapDataPacket.writeVarInt( column.getData().length, buf );
        buf.writeBytes( column.getData() );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.skipBytes( buf.readableBytes() );
    }

    @AllArgsConstructor
    @Getter
    public static class MapDataNew implements MapData
    {

        private int columns;
        private int rows;
        private int x;
        private int y;
        private byte[] data;
    }

    public static interface MapData
    {
    }

}
