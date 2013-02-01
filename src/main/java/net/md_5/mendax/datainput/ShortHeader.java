package net.md_5.mendax.datainput;

import java.io.DataInput;
import java.io.IOException;

class ShortHeader extends Instruction {

    private final Instruction child;

    ShortHeader(Instruction child) {
        this.child = child;
    }

    @Override
    void read(DataInput in, byte[] buffer) throws IOException {
        short size = in.readShort();
        for (short s = 0; s < size; s++) {
            child.read(in, buffer);
        }
    }
}
