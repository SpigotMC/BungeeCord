package net.md_5.bungee.protocol.netty;

import io.netty.buffer.ByteBuf;
import java.io.IOException;

class Item extends Instruction
{

    @Override
    void read(ByteBuf in) throws IOException
    {
        short type = in.readShort();
        if ( type >= 0 )
        {
            in.skipBytes( 3 );
            SHORT_BYTE.read( in );
        }
    }
}
