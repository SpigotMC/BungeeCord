package net.md_5.bungee.protocol.skip;

import io.netty.buffer.ByteBuf;

class ShortHeader extends Instruction
{

    private final Instruction child;

    ShortHeader(Instruction child)
    {
        this.child = child;
    }

    @Override
    void read(ByteBuf in)
    {
        short size = in.readShort();
        for ( short s = 0; s < size; s++ )
        {
            child.read( in );
        }
    }
}
