package net.md_5.bungee.protocol.netty;

import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class BulkChunk extends Instruction
{

    @Override
    void read(ByteBuf in) throws IOException
    {
        short count = in.readShort();
        int size = in.readInt();
        in.readBoolean();
        in.skipBytes( size + count * 12 );
    }
}
