package net.md_5.bungee.protocol.netty;

import io.netty.buffer.ByteBuf;
import java.io.IOException;

class ShortHeader extends Instruction
{

    private final Instruction child;

    ShortHeader(Instruction child)
    {
        this.child = child;
    }

    @Override
    void read(ByteBuf in) throws IOException
    {
        short size = in.readShort();
        for ( short s = 0; s < size; s++ )
        {
            child.read( in );
        }
    }
}
