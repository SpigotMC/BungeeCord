package net.md_5.bungee.protocol.skip;

import io.netty.buffer.ByteBuf;

class OptionalMotion extends Instruction
{

    @Override
    void read(ByteBuf in)
    {
        int data = in.readInt();
        if ( data > 0 )
        {
            in.skipBytes( 6 );
        }
    }
}
