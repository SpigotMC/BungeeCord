package net.md_5.bungee.protocol.skip;

import io.netty.buffer.ByteBuf;

public class BulkChunk extends Instruction
{

    @Override
    void read(ByteBuf in)
    {
        short count = in.readShort();
        int size = in.readInt();
        in.readBoolean();
        in.skipBytes( size + count * 12 );
    }
}
