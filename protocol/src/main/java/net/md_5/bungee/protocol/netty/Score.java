package net.md_5.bungee.protocol.netty;

import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class Score extends Instruction
{

    @Override
    void read(ByteBuf in) throws IOException
    {
        STRING.read( in );
        if ( in.readByte() == 0 )
        {
            STRING.read( in );
            INT.read( in );
        }
    }
}
