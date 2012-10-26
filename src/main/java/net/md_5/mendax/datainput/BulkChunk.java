package net.md_5.mendax.datainput;

import java.io.DataInput;
import java.io.IOException;

public class BulkChunk extends Instruction {

    @Override
    void read(DataInput in) throws IOException {
        short count = in.readShort();
        INT_BYTE.read(in);
        skip(in, count * 12);
    }
}
