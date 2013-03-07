package net.md_5.bungee.protocol.netty;

import io.netty.buffer.ByteBuf;
import java.io.IOException;

class UnsignedShortByte extends Instruction
{

    @Override
    void read(ByteBuf in) throws IOException
    {
        int size = in.readUnsignedShort();
        skip( in, size );
    }
}
