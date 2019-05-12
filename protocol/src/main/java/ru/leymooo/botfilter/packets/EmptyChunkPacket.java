package ru.leymooo.botfilter.packets;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.CompoundTagBuilder;
import com.sk89q.jnbt.NBTOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import ru.leymooo.botfilter.packets.chunk.BlockStorage;
import ru.leymooo.botfilter.packets.chunk.FlexibleStorage;

import java.io.IOException;
import java.util.function.Consumer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of =
        {
                "x", "z"
        })
public class EmptyChunkPacket extends DefinedPacket {

    int x;
    int z;

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int version) {
        buf.writeInt(this.x);
        buf.writeInt(this.z);
        buf.writeBoolean(true);

        //Damn you, 1.14
        if (version >= ProtocolConstants.MINECRAFT_1_14) {
            BlockStorage emptyBlocks = new BlockStorage();
            ByteBuf chunkData = PooledByteBufAllocator.DEFAULT.heapBuffer(0xffff);

            try {
                int mask = writeChunkSections(chunkData, emptyBlocks::write, true);

                writeVarInt(mask, buf);

                CompoundTag tag = CompoundTagBuilder.create()
                        .putLongArray("MOTION_BLOCKING", new long[FlexibleStorage.roundToNearest(256 * 9, 64) / 64])
                        .build(); //1.14 - heightmaps, important thing

                new NBTOutputStream(new ByteBufOutputStream(buf)).writeNamedTag("root", tag);

                writeVarInt(chunkData.readableBytes(), buf);
                buf.writeBytes(chunkData);

                writeVarInt(0, buf);
            } catch (IOException e) {
                throw new RuntimeException("Cannot write NBT tag to buffer.", e);
            } finally {
                chunkData.release();
            }
            return;
        }

        if (version == ProtocolConstants.MINECRAFT_1_8) {
            buf.writeShort(1);
        } else {
            writeVarInt(0, buf);
        }
        if (version < ProtocolConstants.MINECRAFT_1_13) {
            writeArray(new byte[256], buf); //1.8 - 1.12.2
        } else if (version == ProtocolConstants.MINECRAFT_1_13) {
            writeArray(new byte[512], buf); //1.13
        } else {
            writeArray(new byte[1024], buf);
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
    }

    private static int writeChunkSections(ByteBuf out, Consumer<ByteBuf> storageWriter, boolean fullChunk) {
        int mask = 0;
        for (int index = 0; index < 16; index++) {
            mask |= 1 << index;
            out.writeShort(0); //1.14 - number of non-air blocks
            storageWriter.accept(out); //write block storage
        }

        if (fullChunk) { //Write biomes if chunk is full
            for (int i = 0; i < 256; i++) {
                out.writeInt(127);
            }
        }

        return mask;
    }

}
