package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MinecraftInput
{

    private final ByteBuf buf;

    public byte readByte()
    {
        return buf.readByte();
    }

    public short readUnsignedByte()
    {
        return buf.readUnsignedByte();
    }

    public int readInt()
    {
        return buf.readInt();
    }

    public String readString()
    {
        short len = buf.readShort();
        char[] c = new char[ len ];
        for ( int i = 0; i < c.length; i++ )
        {
            c[i] = buf.readChar();
        }

        return new String( c );
    }
}
