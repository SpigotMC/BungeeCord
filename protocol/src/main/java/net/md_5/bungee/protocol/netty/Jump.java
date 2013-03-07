package net.md_5.bungee.protocol.netty;

import io.netty.buffer.ByteBuf;
import java.io.IOException;

class Jump extends Instruction
{

    final int len;

    Jump(int len)
    {
        if ( len < 0 )
        {
            throw new IndexOutOfBoundsException();
        }
        this.len = len;
    }

    @Override
    void read(ByteBuf in) throws IOException
    {
        skip( in, len );
    }
}
