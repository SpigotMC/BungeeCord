package net.md_5.mendax.datainput;

import java.io.DataInput;
import java.io.IOException;

class MetaData extends Instruction {

    @Override
    void read(DataInput in, byte[] buffer) throws IOException {
        int x = in.readUnsignedByte();
        while (x != 127) {
            int type = x >> 5;
            switch (type) {
                case 0:
                    BYTE.read(in, buffer);
                    break;
                case 1:
                    SHORT.read(in, buffer);
                    break;
                case 2:
                    INT.read(in, buffer);
                    break;
                case 3:
                    FLOAT.read(in, buffer);
                    break;
                case 4:
                    STRING.read(in, buffer);
                    break;
                case 5:
                    ITEM.read(in, buffer);
                    break;
                case 6:
                    skip(in, buffer, 12); //  int, int, int
                    break;
                default:
                    throw new IllegalArgumentException("Unknown metadata type " + type);
            }
            x = in.readUnsignedByte();
        }
    }
}
