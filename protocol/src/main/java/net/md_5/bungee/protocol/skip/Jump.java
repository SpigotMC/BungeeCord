package net.md_5.bungee.protocol.skip;

import io.netty.buffer.ByteBuf;

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
    void read(ByteBuf in)
    {
        in.skipBytes( len );
    }
}
