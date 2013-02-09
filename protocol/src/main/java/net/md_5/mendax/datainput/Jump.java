package net.md_5.mendax.datainput;

import java.io.DataInput;
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
    void read(DataInput in, byte[] buffer) throws IOException
    {
        skip( in, buffer, len );
    }
}
