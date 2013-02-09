package net.md_5.mendax.datainput;

import java.io.DataInput;
import java.io.IOException;

class IntHeader extends Instruction {

    private final Instruction child;

    IntHeader(Instruction child) {
        this.child = child;
    }

    @Override
    void read(DataInput in, byte[] buffer) throws IOException {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            child.read(in, buffer);
        }
    }
}
