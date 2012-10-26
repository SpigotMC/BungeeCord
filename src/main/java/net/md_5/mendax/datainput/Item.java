package net.md_5.mendax.datainput;

import java.io.DataInput;
import java.io.IOException;

class Item extends Instruction {

    @Override
    void read(DataInput in) throws IOException {
        short type = in.readShort();
        if (type >= 0) {
            skip(in, 3);
            SHORT_BYTE.read(in);
        }
    }
}
