package net.md_5.bungee.protocol.packet.extra;

import io.netty.buffer.ByteBuf;
import java.util.List;
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
public class MultiBlockChange extends DefinedPacket
{

    private List<Block> blocks;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int version)
    {
        int chunkX = blocks.get( 0 ).getBlockX() >> 4;
        int chunkZ = blocks.get( 0 ).getBlockZ() >> 4;
        buf.writeInt( chunkX );
        buf.writeInt( chunkZ );
        MultiBlockChange.writeVarInt( blocks.size(), buf );
        for ( Block block : blocks )
        {
            buf.writeShort( ( block.getBlockX() - ( chunkX << 4 ) ) << 12 | ( block.getBlockZ() - ( chunkZ << 4 ) ) << 8 | block.getBlockY() );
            block.writeBlock( buf );
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

    @Data
    @AllArgsConstructor
    public static class Block
    {

        private int blockId;
        private int blockData;
        private int blockX;
        private int blockY;
        private int blockZ;

        public void writeBlock(ByteBuf buf)
        {
            MultiBlockChange.writeVarInt( ( blockId << 4 ) | ( blockData & 0xF ), buf );
        }

    }

}
