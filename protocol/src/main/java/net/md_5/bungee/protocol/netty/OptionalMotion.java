package net.md_5.bungee.protocol.netty;

import io.netty.buffer.ByteBuf;
import java.io.IOException;

class OptionalMotion extends Instruction
{

    @Override
    void read(ByteBuf in) throws IOException
    {
        int data = in.readInt();
        if ( data > 0 )
        {
            in.skipBytes( 6 );
        }
    }
}
