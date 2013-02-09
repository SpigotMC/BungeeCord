package net.md_5.mendax.datainput;

import java.io.DataInput;
import java.io.IOException;

class ByteHeader extends Instruction
{

    private final Instruction child;

    ByteHeader(Instruction child)
    {
        this.child = child;
    }

    @Override
    void read(DataInput in, byte[] buffer) throws IOException
    {
        byte size = in.readByte();
        for ( byte b = 0; b < size; b++ )
        {
            child.read( in, buffer );
        }
    }
}
