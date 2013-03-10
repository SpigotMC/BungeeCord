package net.md_5.mendax.datainput;

import java.io.DataInput;
import java.io.IOException;

class Team extends Instruction
{

    @Override
    void read(DataInput in, byte[] buffer) throws IOException
    {
        STRING.read( in, buffer );
        byte mode = in.readByte();
        if ( mode == 0 || mode == 2 )
        {
            STRING.read( in, buffer );
            STRING.read( in, buffer );
            STRING.read( in, buffer );
            BYTE.read( in, buffer );
        }
        if ( mode == 0 || mode == 3 || mode == 4 )
        {
            STRING_ARRAY.read( in, buffer );
        }
    }
}
