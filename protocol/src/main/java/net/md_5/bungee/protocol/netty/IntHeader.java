package net.md_5.bungee.protocol.netty;

import io.netty.buffer.ByteBuf;
import java.io.IOException;

class IntHeader extends Instruction
{

    private final Instruction child;

    IntHeader(Instruction child)
    {
        this.child = child;
    }

    @Override
    void read(ByteBuf in) throws IOException
    {
        int size = in.readInt();
        for ( int i = 0; i < size; i++ )
        {
            child.read( in );
        }
    }
}
