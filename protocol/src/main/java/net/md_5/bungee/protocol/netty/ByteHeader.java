package net.md_5.bungee.protocol.netty;

import io.netty.buffer.ByteBuf;
import java.io.IOException;

class ByteHeader extends Instruction
{

    private final Instruction child;

    ByteHeader(Instruction child)
    {
        this.child = child;
    }

    @Override
    void read(ByteBuf in) throws IOException
    {
        byte size = in.readByte();
        for ( byte b = 0; b < size; b++ )
        {
            child.read( in );
        }
    }
}
