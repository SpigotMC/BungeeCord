package net.md_5.mendax.datainput;

import java.io.DataInput;
import java.io.IOException;

class MetaData extends Instruction {

    @Override
    void read(DataInput in) throws IOException {
        int x = in.readUnsignedByte();
        while (x != 127) {
            int type = x >> 5;
            switch (type) {
                case 0:
                    BYTE.read(in);
                    break;
                case 1:
                    SHORT.read(in);
                    break;
                case 2:
                    INT.read(in);
                    break;
                case 3:
                    FLOAT.read(in);
                    break;
                case 4:
                    STRING.read(in);
                    break;
                case 5:
                    if (in.readShort() > 0) {
                        skip(in, 3);
                        short len = in.readShort();
                        if (len > 0 ) {
                            skip(in, len);
                        }
                    }
                    break;
                case 6:
                    skip(in, 12); //  int, int, int
                    break;
                default:
                    throw new IllegalArgumentException("Unknown metadata type " + type);
            }
            x = in.readUnsignedByte();
        }
    }
}
