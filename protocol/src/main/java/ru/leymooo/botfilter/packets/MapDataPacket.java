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

    //diffs:
    //1.8
    //1.9 - 1.13
    //1.14 - 1.16
    //1.17 - xxxx
    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        MapDataPacket.writeVarInt( this.mapId, buf );
        buf.writeByte( this.scale );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 && protocolVersion < ProtocolConstants.MINECRAFT_1_17 )
        {
            buf.writeBoolean( false );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_14 )
        {
            buf.writeBoolean( false );
        }

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_17 )
        {
            buf.writeBoolean( false );
        } else
        {
            MapDataPacket.writeVarInt( 0, buf );
        }
        buf.writeByte( data.getColumns() );
        buf.writeByte( data.getRows() );
        buf.writeByte( data.getX() );
        buf.writeByte( data.getY() );
        buf.ensureWritable( 3 + data.getData().length );
        writeArray( data.getData(), buf );
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
