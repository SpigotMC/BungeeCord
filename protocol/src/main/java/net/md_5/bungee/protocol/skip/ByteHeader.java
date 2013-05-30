package net.md_5.bungee.protocol.skip;

import io.netty.buffer.ByteBuf;

class ByteHeader extends Instruction
{

    private final Instruction child;

    ByteHeader(Instruction child)
    {
        this.child = child;
    }

    @Override
    void read(ByteBuf in)
    {
        byte size = in.readByte();
        for ( byte b = 0; b < size; b++ )
        {
            child.read( in );
        }
    }
}
