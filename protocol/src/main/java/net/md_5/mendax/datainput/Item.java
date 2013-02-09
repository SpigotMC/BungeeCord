package net.md_5.mendax.datainput;

import java.io.DataInput;
import java.io.IOException;

class Item extends Instruction
{

    @Override
    void read(DataInput in, byte[] buffer) throws IOException
    {
        short type = in.readShort();
        if ( type >= 0 )
        {
            skip( in, buffer, 3 );
            SHORT_BYTE.read( in, buffer );
        }
    }
}
