package net.md_5.bungee.protocol.netty;

import io.netty.buffer.ByteBuf;
import java.io.IOException;

class Team extends Instruction
{

    @Override
    void read(ByteBuf in) throws IOException
    {
        STRING.read( in );
        byte mode = in.readByte();
        if ( mode == 0 || mode == 2 )
        {
            STRING.read( in );
            STRING.read( in );
            STRING.read( in );
            BYTE.read( in );
        }
        if ( mode == 0 || mode == 3 || mode == 4 )
        {
            STRING_ARRAY.read( in );
        }
    }
}
