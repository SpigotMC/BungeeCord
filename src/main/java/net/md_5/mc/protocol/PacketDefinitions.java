package net.md_5.mc.protocol;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacketDefinitions {

    private static final Instruction[][] opCodes = new Instruction[256][];
    private static final Instruction BYTE = new JumpOpCode(1);
    private static final Instruction BOOLEAN = BYTE;
    private static final Instruction SHORT = new JumpOpCode(2);
    private static final Instruction INT = new JumpOpCode(4);
    private static final Instruction FLOAT = INT;
    private static final Instruction LONG = new JumpOpCode(8);
    private static final Instruction DOUBLE = LONG;
    private static final Instruction SHORT_BYTE = new ShortHeader(BYTE);
    private static final Instruction BYTE_INT = new ByteHeader(INT);
    private static final Instruction INT_BYTE = new IntHeader(BYTE);
    private static final Instruction INT_3 = new IntHeader(new JumpOpCode(3));
    private static final Instruction STRING = new ShortHeader(SHORT);
    private static final Instruction ITEM = new Instruction() {
        @Override
        void read(DataInput in) throws IOException {
            short type = in.readShort();
            if (type >= 0) {
                skip(in, 3);
                SHORT_BYTE.read(in);
            }
        }

        @Override
        public String toString() {
            return "Item";
        }
    };
    private static final Instruction SHORT_ITEM = new ShortHeader(ITEM);
    private static final Instruction METADATA = new Instruction() {
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
                        skip(in, 5); // short, byte, short
                        break;
                    case 6:
                        skip(in, 6); //  int, int, int
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown metadata type " + type);
                }
                x = in.readByte();
            }
        }

        @Override
        public String toString() {
            return "Metadata";
        }
    };
    private static final Instruction BULK_CHUNK = new Instruction() {
        @Override
        void read(DataInput in) throws IOException {
            short count = in.readShort();
            INT_BYTE.read(in);
            skip(in, count * 12);
        }

        @Override
        public String toString() {
            return "Bulk Chunk";
        }
    };
    private static final Instruction UBYTE_BYTE = new Instruction() {
        @Override
        void read(DataInput in) throws IOException {
            int size = in.readUnsignedByte();
            skip(in, size);
        }

        @Override
        public String toString() {
            return "Unsigned Byte Byte";
        }
    };

    static {
        opCodes[0x00] = new Instruction[]{INT};
        opCodes[0x01] = new Instruction[]{INT, STRING, BYTE, BYTE, BYTE, BYTE, BYTE};
        opCodes[0x02] = new Instruction[]{BYTE, STRING, STRING, INT};
        opCodes[0x03] = new Instruction[]{STRING};
        opCodes[0x04] = new Instruction[]{LONG};
        opCodes[0x05] = new Instruction[]{INT, SHORT, ITEM};
        opCodes[0x06] = new Instruction[]{INT, INT, INT};
        opCodes[0x07] = new Instruction[]{INT, INT, BOOLEAN};
        opCodes[0x08] = new Instruction[]{SHORT, SHORT, FLOAT};
        opCodes[0x09] = new Instruction[]{INT, BYTE, BYTE, SHORT, STRING};
        opCodes[0x0A] = new Instruction[]{BOOLEAN};
        opCodes[0x0B] = new Instruction[]{DOUBLE, DOUBLE, DOUBLE, DOUBLE, BOOLEAN};
        opCodes[0x0C] = new Instruction[]{FLOAT, FLOAT, BOOLEAN};
        opCodes[0x0D] = new Instruction[]{DOUBLE, DOUBLE, DOUBLE, DOUBLE, FLOAT, FLOAT, BOOLEAN};
        opCodes[0x0E] = new Instruction[]{BYTE, INT, BYTE, INT, BYTE};
        opCodes[0x0F] = new Instruction[]{INT, BYTE, INT, BYTE, ITEM, BYTE, BYTE, BYTE};
        opCodes[0x10] = new Instruction[]{SHORT};
        opCodes[0x11] = new Instruction[]{INT, BYTE, INT, BYTE, INT};
        opCodes[0x12] = new Instruction[]{INT, BYTE};
        opCodes[0x13] = new Instruction[]{INT, BYTE};
        opCodes[0x14] = new Instruction[]{INT, STRING, INT, INT, INT, BYTE, BYTE, SHORT, METADATA};
        opCodes[0x15] = new Instruction[]{INT, SHORT, BYTE, SHORT, INT, INT, INT, BYTE, BYTE, BYTE};
        opCodes[0x16] = new Instruction[]{INT, INT};
        opCodes[0x17] = new Instruction[]{INT, BYTE, INT, INT, INT, INT, SHORT, SHORT, SHORT};
        opCodes[0x18] = new Instruction[]{INT, BYTE, INT, INT, INT, BYTE, BYTE, BYTE, SHORT, SHORT, SHORT, METADATA};
        opCodes[0x19] = new Instruction[]{INT, STRING, INT, INT, INT, INT};
        opCodes[0x1A] = new Instruction[]{INT, INT, INT, INT, SHORT};
        opCodes[0x1B] = null; // Does not exist
        opCodes[0x1C] = new Instruction[]{INT, SHORT, SHORT, SHORT};
        opCodes[0x1D] = new Instruction[]{BYTE_INT};
        opCodes[0x1E] = new Instruction[]{INT};
        opCodes[0x1F] = new Instruction[]{INT, BYTE, BYTE, BYTE};
        opCodes[0x20] = new Instruction[]{INT, BYTE, BYTE};
        opCodes[0x21] = new Instruction[]{INT, BYTE, BYTE, BYTE, BYTE, BYTE};
        opCodes[0x22] = new Instruction[]{INT, INT, INT, INT, BYTE, BYTE};
        opCodes[0x23] = new Instruction[]{INT, BYTE};
        opCodes[0x24] = null; // Does not exist
        opCodes[0x25] = null; // Does not exist
        opCodes[0x26] = new Instruction[]{INT, BYTE};
        opCodes[0x27] = new Instruction[]{INT, INT};
        opCodes[0x28] = new Instruction[]{INT, METADATA};
        opCodes[0x29] = new Instruction[]{INT, BYTE, BYTE, SHORT};
        opCodes[0x2A] = new Instruction[]{INT, BYTE};
        opCodes[0x2B] = new Instruction[]{FLOAT, SHORT, SHORT};
        //
        //
        // 0x2C -> 0x32 Do not exist
        //
        //
        opCodes[0x33] = new Instruction[]{INT, INT, BOOLEAN, SHORT, SHORT, INT_BYTE};
        opCodes[0x34] = new Instruction[]{INT, INT, SHORT, INT_BYTE};
        opCodes[0x35] = new Instruction[]{INT, BYTE, INT, SHORT, BYTE};
        opCodes[0x36] = new Instruction[]{INT, SHORT, INT, BYTE, BYTE, SHORT};
        opCodes[0x37] = new Instruction[]{INT, INT, INT, INT, BYTE};
        opCodes[0x38] = new Instruction[]{BULK_CHUNK};
        opCodes[0x39] = null; // Does not exist
        opCodes[0x3A] = null; // Does not exist
        opCodes[0x3B] = null; // Does not exist
        opCodes[0x3C] = new Instruction[]{DOUBLE, DOUBLE, DOUBLE, FLOAT, INT_3, FLOAT, FLOAT, FLOAT};
        opCodes[0x3D] = new Instruction[]{INT, INT, BYTE, INT, INT};
        opCodes[0x3E] = new Instruction[]{STRING, INT, INT, INT, FLOAT, BYTE};
        //
        //
        // 0x3F -> 0x45 Do not exist
        //
        //
        opCodes[0x46] = new Instruction[]{BYTE, BYTE};
        opCodes[0x47] = new Instruction[]{INT, BOOLEAN, INT, INT, INT};
        //
        //
        // 0x4A -> 0x63 Do not exist
        //
        //
        opCodes[0x64] = new Instruction[]{BYTE, BYTE, STRING, BYTE};
        opCodes[0x65] = new Instruction[]{BYTE};
        opCodes[0x66] = new Instruction[]{BYTE, SHORT, BOOLEAN, SHORT, BOOLEAN, ITEM};
        opCodes[0x67] = new Instruction[]{BYTE, SHORT, ITEM};
        opCodes[0x68] = new Instruction[]{BYTE, SHORT_ITEM};
        opCodes[0x69] = new Instruction[]{BYTE, SHORT, SHORT};
        opCodes[0x6A] = new Instruction[]{BYTE, SHORT, BOOLEAN};
        opCodes[0x6B] = new Instruction[]{SHORT, ITEM};
        opCodes[0x6C] = new Instruction[]{BYTE, BYTE};
        //
        //
        // 0x6D -> 0x81 Do not exist
        //
        //
        opCodes[0x82] = new Instruction[]{INT, SHORT, INT, STRING, STRING, STRING, STRING};
        opCodes[0x83] = new Instruction[]{SHORT, SHORT, UBYTE_BYTE};
        opCodes[0x84] = new Instruction[]{INT, SHORT, INT, BYTE, SHORT_BYTE};
        //
        //
        // 0x85 -> 0xC7 Do not exist
        //
        //
        opCodes[0xC8] = new Instruction[]{INT, BYTE};
        opCodes[0xC9] = new Instruction[]{STRING, BOOLEAN, SHORT};
        opCodes[0xCA] = new Instruction[]{BYTE, BYTE, BYTE};
        opCodes[0xCB] = new Instruction[]{STRING};
        opCodes[0xCC] = new Instruction[]{STRING, BYTE, BYTE, BYTE};
        opCodes[0xCD] = new Instruction[]{BYTE};
        //
        //
        // 0xCE -> 0xF9 Do not exist
        //
        //
        opCodes[0xFA] = new Instruction[]{STRING, SHORT_BYTE};
        opCodes[0xFB] = null; // Does not exist
        opCodes[0xFC] = new Instruction[]{SHORT_BYTE, SHORT_BYTE};
        opCodes[0xFD] = new Instruction[]{STRING, SHORT_BYTE, SHORT_BYTE};
        opCodes[0xFE] = new Instruction[]{};
        opCodes[0xFF] = new Instruction[]{STRING};

        crushInstructions();
    }

    private static void crushInstructions() {
        for (int i = 0; i < opCodes.length; i++) {
            Instruction[] instructions = opCodes[i];
            if (instructions != null) {
                List<Instruction> crushed = new ArrayList<Instruction>();
                int nextJumpSize = 0;
                for (Instruction child : instructions) {
                    if (child instanceof JumpOpCode) {
                        nextJumpSize += ((JumpOpCode) child).len;
                    } else {
                        if (nextJumpSize != 0) {
                            crushed.add(new JumpOpCode(nextJumpSize));
                        }
                        crushed.add(child);
                        nextJumpSize = 0;
                    }
                }
                if (nextJumpSize != 0) {
                    crushed.add(new JumpOpCode(nextJumpSize));
                }
                opCodes[i] = crushed.toArray(new Instruction[crushed.size()]);
            }
        }
    }

    public static void readPacket(DataInput in) throws IOException {
        int packetId = in.readUnsignedByte();
        Instruction[] instructions = opCodes[packetId];
        if (instructions == null) {
            throw new IOException("Unknown packet id " + packetId);
        }

        for (Instruction instruction : instructions) {
            instruction.read(in);
        }
    }

    static abstract class Instruction {

        abstract void read(DataInput in) throws IOException;

        final void skip(DataInput in, int len) throws IOException {
            for (int i = 0; i < len; i++) {
                in.readUnsignedByte();
            }
        }

        @Override
        public abstract String toString();
    }

    static class JumpOpCode extends Instruction {

        private final int len;

        public JumpOpCode(int len) {
            if (len < 0) {
                throw new IndexOutOfBoundsException();
            }
            this.len = len;
        }

        @Override
        void read(DataInput in) throws IOException {
            skip(in, len);
        }

        @Override
        public String toString() {
            return "Jump(" + len + ")";
        }
    }

    static class ByteHeader extends Instruction {

        private final Instruction child;

        public ByteHeader(Instruction child) {
            this.child = child;
        }

        @Override
        void read(DataInput in) throws IOException {
            byte size = in.readByte();
            for (byte b = 0; b < size; b++) {
                child.read(in);
            }
        }

        @Override
        public String toString() {
            return "ByteHeader(" + child + ")";
        }
    }

    static class ShortHeader extends Instruction {

        private final Instruction child;

        public ShortHeader(Instruction child) {
            this.child = child;
        }

        @Override
        void read(DataInput in) throws IOException {
            short size = in.readShort();
            for (short s = 0; s < size; s++) {
                child.read(in);
            }
        }

        @Override
        public String toString() {
            return "ShortHeader(" + child + ")";
        }
    }

    static class IntHeader extends Instruction {

        private final Instruction child;

        public IntHeader(Instruction child) {
            this.child = child;
        }

        @Override
        void read(DataInput in) throws IOException {
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                child.read(in);
            }
        }

        @Override
        public String toString() {
            return "IntHeader(" + child + ")";
        }
    }
}
