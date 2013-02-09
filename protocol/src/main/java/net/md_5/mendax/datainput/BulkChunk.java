package net.md_5.mendax.datainput;

import java.io.DataInput;
import java.io.IOException;

public class BulkChunk extends Instruction {

    @Override
    void read(DataInput in, byte[] buffer) throws IOException {
        short count = in.readShort();
        int size = in.readInt();
        in.readBoolean();
        skip(in, buffer, size + count * 12);
    }
}
