package net.md_5.bungee.protocol.skip;

import io.netty.buffer.ByteBuf;

public class OptionalWindow extends Instruction
{

    @Override
    void read(ByteBuf in)
    {
        BYTE.read( in );
        byte type = in.readByte();
        STRING.read( in );
        BYTE.read( in );
        BOOLEAN.read( in );
        if ( type == 11 )
        {
            INT.read( in );
        }
    }
}
