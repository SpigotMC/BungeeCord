package net.md_5.mendax.datainput;

import java.io.DataInput;
import java.io.IOException;

public class UnsignedShortByte extends Instruction {

    @Override
    void read(DataInput in, byte[] buffer) throws IOException {
        int size = in.readUnsignedShort();
        skip(in, buffer, size);
    }
}
