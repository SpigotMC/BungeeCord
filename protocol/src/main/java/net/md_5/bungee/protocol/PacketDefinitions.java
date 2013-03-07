package net.md_5.bungee.protocol;

import static net.md_5.bungee.protocol.PacketDefinitions.OpCode.*;

public class PacketDefinitions
{

    public static final OpCode[][] opCodes = new OpCode[ 512 ][];
    public static final int VANILLA_PROTOCOL = 0;
    public static final int FORGE_PROTOCOL = 256;

    public enum OpCode
    {

        BOOLEAN, BULK_CHUNK, BYTE, BYTE_INT, DOUBLE, FLOAT, INT, INT_3, INT_BYTE, ITEM, LONG, METADATA, OPTIONAL_MOTION, SHORT, SHORT_BYTE, SHORT_ITEM, STRING, USHORT_BYTE
    }

    static
    {
        opCodes[0x00] = new OpCode[]
        {
            INT
        };
        opCodes[0x01] = new OpCode[]
        {
            INT, STRING, BYTE, BYTE, BYTE, BYTE, BYTE
        };
        opCodes[0x02] = new OpCode[]
        {
            BYTE, STRING, STRING, INT
        };
        opCodes[0x03] = new OpCode[]
        {
            STRING
        };
        opCodes[0x04] = new OpCode[]
        {
            LONG, LONG
        };
        opCodes[0x05] = new OpCode[]
        {
            INT, SHORT, ITEM
        };
        opCodes[0x06] = new OpCode[]
        {
            INT, INT, INT
        };
        opCodes[0x07] = new OpCode[]
        {
            INT, INT, BOOLEAN
        };
        opCodes[0x08] = new OpCode[]
        {
            SHORT, SHORT, FLOAT
        };
        opCodes[0x09] = new OpCode[]
        {
            INT, BYTE, BYTE, SHORT, STRING
        };
        opCodes[0x0A] = new OpCode[]
        {
            BOOLEAN
        };
        opCodes[0x0B] = new OpCode[]
        {
            DOUBLE, DOUBLE, DOUBLE, DOUBLE, BOOLEAN
        };
        opCodes[0x0C] = new OpCode[]
        {
            FLOAT, FLOAT, BOOLEAN
        };
        opCodes[0x0D] = new OpCode[]
        {
            DOUBLE, DOUBLE, DOUBLE, DOUBLE, FLOAT, FLOAT, BOOLEAN
        };
        opCodes[0x0E] = new OpCode[]
        {
            BYTE, INT, BYTE, INT, BYTE
        };
        opCodes[0x0F] = new OpCode[]
        {
            INT, BYTE, INT, BYTE, ITEM, BYTE, BYTE, BYTE
        };
        opCodes[0x10] = new OpCode[]
        {
            SHORT
        };
        opCodes[0x11] = new OpCode[]
        {
            INT, BYTE, INT, BYTE, INT
        };
        opCodes[0x12] = new OpCode[]
        {
            INT, BYTE
        };
        opCodes[0x13] = new OpCode[]
        {
            INT, BYTE
        };
        opCodes[0x14] = new OpCode[]
        {
            INT, STRING, INT, INT, INT, BYTE, BYTE, SHORT, METADATA
        };
        opCodes[0x16] = new OpCode[]
        {
            INT, INT
        };
        opCodes[0x17] = new OpCode[]
        {
            INT, BYTE, INT, INT, INT, BYTE, BYTE, OPTIONAL_MOTION
        };
        opCodes[0x18] = new OpCode[]
        {
            INT, BYTE, INT, INT, INT, BYTE, BYTE, BYTE, SHORT, SHORT, SHORT, METADATA
        };
        opCodes[0x19] = new OpCode[]
        {
            INT, STRING, INT, INT, INT, INT
        };
        opCodes[0x1A] = new OpCode[]
        {
            INT, INT, INT, INT, SHORT
        };
        opCodes[0x1C] = new OpCode[]
        {
            INT, SHORT, SHORT, SHORT
        };
        opCodes[0x1D] = new OpCode[]
        {
            BYTE_INT
        };
        opCodes[0x1E] = new OpCode[]
        {
            INT
        };
        opCodes[0x1F] = new OpCode[]
        {
            INT, BYTE, BYTE, BYTE
        };
        opCodes[0x20] = new OpCode[]
        {
            INT, BYTE, BYTE
        };
        opCodes[0x21] = new OpCode[]
        {
            INT, BYTE, BYTE, BYTE, BYTE, BYTE
        };
        opCodes[0x22] = new OpCode[]
        {
            INT, INT, INT, INT, BYTE, BYTE
        };
        opCodes[0x23] = new OpCode[]
        {
            INT, BYTE
        };
        opCodes[0x26] = new OpCode[]
        {
            INT, BYTE
        };
        opCodes[0x27] = new OpCode[]
        {
            INT, INT
        };
        opCodes[0x28] = new OpCode[]
        {
            INT, METADATA
        };
        opCodes[0x29] = new OpCode[]
        {
            INT, BYTE, BYTE, SHORT
        };
        opCodes[0x2A] = new OpCode[]
        {
            INT, BYTE
        };
        opCodes[0x2B] = new OpCode[]
        {
            FLOAT, SHORT, SHORT
        };
        opCodes[0x33] = new OpCode[]
        {
            INT, INT, BOOLEAN, SHORT, SHORT, INT_BYTE
        };
        opCodes[0x34] = new OpCode[]
        {
            INT, INT, SHORT, INT_BYTE
        };
        opCodes[0x35] = new OpCode[]
        {
            INT, BYTE, INT, SHORT, BYTE
        };
        opCodes[0x36] = new OpCode[]
        {
            INT, SHORT, INT, BYTE, BYTE, SHORT
        };
        opCodes[0x37] = new OpCode[]
        {
            INT, INT, INT, INT, BYTE
        };
        opCodes[0x38] = new OpCode[]
        {
            BULK_CHUNK
        };
        opCodes[0x3C] = new OpCode[]
        {
            DOUBLE, DOUBLE, DOUBLE, FLOAT, INT_3, FLOAT, FLOAT, FLOAT
        };
        opCodes[0x3D] = new OpCode[]
        {
            INT, INT, BYTE, INT, INT, BOOLEAN
        };
        opCodes[0x3E] = new OpCode[]
        {
            STRING, INT, INT, INT, FLOAT, BYTE
        };
        opCodes[0x46] = new OpCode[]
        {
            BYTE, BYTE
        };
        opCodes[0x47] = new OpCode[]
        {
            INT, BYTE, INT, INT, INT
        };
        opCodes[0x64] = new OpCode[]
        {
            BYTE, BYTE, STRING, BYTE
        };
        opCodes[0x65] = new OpCode[]
        {
            BYTE
        };
        opCodes[0x66] = new OpCode[]
        {
            BYTE, SHORT, BYTE, SHORT, BOOLEAN, ITEM
        };
        opCodes[0x67] = new OpCode[]
        {
            BYTE, SHORT, ITEM
        };
        opCodes[0x68] = new OpCode[]
        {
            BYTE, SHORT_ITEM
        };
        opCodes[0x69] = new OpCode[]
        {
            BYTE, SHORT, SHORT
        };
        opCodes[0x6A] = new OpCode[]
        {
            BYTE, SHORT, BOOLEAN
        };
        opCodes[0x6B] = new OpCode[]
        {
            SHORT, ITEM
        };
        opCodes[0x6C] = new OpCode[]
        {
            BYTE, BYTE
        };
        opCodes[0x82] = new OpCode[]
        {
            INT, SHORT, INT, STRING, STRING, STRING, STRING
        };
        opCodes[0x83] = new OpCode[]
        {
            SHORT, SHORT, USHORT_BYTE
        };
        opCodes[0x84] = new OpCode[]
        {
            INT, SHORT, INT, BYTE, SHORT_BYTE
        };
        opCodes[0xC3] = new OpCode[]
        {
            SHORT, SHORT, INT_BYTE
        };
        opCodes[0xC8] = new OpCode[]
        {
            INT, BYTE
        };
        opCodes[0xC9] = new OpCode[]
        {
            STRING, BOOLEAN, SHORT
        };
        opCodes[0xCA] = new OpCode[]
        {
            BYTE, BYTE, BYTE
        };
        opCodes[0xCB] = new OpCode[]
        {
            STRING
        };
        opCodes[0xCC] = new OpCode[]
        {
            STRING, BYTE, BYTE, BYTE, BOOLEAN
        };
        opCodes[0xCD] = new OpCode[]
        {
            BYTE
        };
        opCodes[0xFA] = new OpCode[]
        {
            STRING, SHORT_BYTE
        };
        opCodes[0xFC] = new OpCode[]
        {
            SHORT_BYTE, SHORT_BYTE
        };
        opCodes[0xFD] = new OpCode[]
        {
            STRING, SHORT_BYTE, SHORT_BYTE
        };
        opCodes[0xFE] = new OpCode[]
        {
            BYTE
        };
        opCodes[0xFF] = new OpCode[]
        {
            STRING
        };
        /*========================== Minecraft Forge ===========================*/
        opCodes[0x01 + FORGE_PROTOCOL] = new OpCode[]
        {
            INT, STRING, BYTE, INT, BYTE, BYTE, BYTE
        };
    }
}
